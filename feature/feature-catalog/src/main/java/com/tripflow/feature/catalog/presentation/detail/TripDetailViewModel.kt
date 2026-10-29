package com.tripflow.feature.catalog.presentation.detail

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.model.UiState
import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.core.network.review.dto.ReviewResponseDTO
import com.tripflow.feature.catalog.domain.repository.CatalogRepository
import com.tripflow.feature.catalog.domain.repository.ReviewRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class TripDetailData(
    val trip: TripResponseDTO,
    val reviews: List<ReviewResponseDTO>,
    val organizerName: String,
    val isOrganizer: Boolean = false
)

class TripDetailViewModel(
    private val catalogRepository: CatalogRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<TripDetailData>>(UiState.Loading)
    val uiState: StateFlow<UiState<TripDetailData>> = _uiState.asStateFlow()

    private val _isOrganizer = MutableStateFlow(false)
    val isOrganizer: StateFlow<Boolean> = _isOrganizer.asStateFlow()

    private var lastTripId: String? = null

    fun loadTripDetails(tripId: String) {
        lastTripId = tripId
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            val tripDeferred = async { catalogRepository.getTripById(tripId) }
            val reviewsDeferred = async { reviewRepository.getReviewsByTripId(tripId) }
            val meDeferred = async { authRepository.getMe() }

            val tripResult = tripDeferred.await()
            val reviewsResult = reviewsDeferred.await()
            val meResult = meDeferred.await().getOrNull()

            if (tripResult.isFailure) {
                _uiState.value = UiState.Error("Impossibile caricare i dettagli del viaggio")
                return@launch
            }

            reviewsResult.fold(
                onSuccess = { reviews ->
                    val trip = tripResult.getOrThrow()
                    val organizerName = authRepository
                        .getPublicUserByKeycloakId(trip.organizerId)
                        .getOrNull()
                        ?.let { "${it.firstName} ${it.lastName}" }
                        ?: "Organizzatore"

                    val currentToken = ApiClient.tokenProvider?.invoke()
                    val currentSub = extractSub(currentToken)
                    val isOrganizer = (currentSub != null && currentSub == trip.organizerId) ||
                        (meResult != null && (meResult.id == trip.organizerId || meResult.role.equals("ORGANIZER", ignoreCase = true)))

                    _isOrganizer.value = isOrganizer
                    _uiState.value = UiState.Success(
                        TripDetailData(
                            trip = trip,
                            reviews = reviews,
                            organizerName = organizerName,
                            isOrganizer = isOrganizer
                        )
                    )
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(
                        error.message?.takeIf { it.isNotBlank() } ?: "Impossibile caricare le recensioni del viaggio"
                    )
                }
            )
        }
    }

    fun deleteTrip(tripId: String, onDeleted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            catalogRepository.deleteTrip(tripId).fold(
                onSuccess = { onDeleted() },
                onFailure = { e ->
                    onError(e.message ?: "Impossibile eliminare il viaggio")
                }
            )
        }
    }

    fun refreshAfterActivityAdded() {
        lastTripId?.let { loadTripDetails(it) }
    }

    private fun extractSub(token: String?): String? {
        val payload = token?.split('.')?.getOrNull(1) ?: return null
        return try {
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val json = String(decoded, Charsets.UTF_8)
            JSONObject(json).optString("sub").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
