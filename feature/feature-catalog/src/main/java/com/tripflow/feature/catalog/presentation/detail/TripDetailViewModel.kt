package com.tripflow.feature.catalog.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.core.network.review.dto.ReviewResponseDTO
import com.tripflow.feature.catalog.domain.repository.CatalogRepository
import com.tripflow.feature.catalog.domain.repository.ReviewRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripDetailData(
    val trip: TripResponseDTO,
    val reviews: List<ReviewResponseDTO>
)

class TripDetailViewModel(
    private val catalogRepository: CatalogRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<TripDetailData>>(UiState.Loading)
    val uiState: StateFlow<UiState<TripDetailData>> = _uiState.asStateFlow()

    fun loadTripDetails(tripId: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            val tripDeferred = async { catalogRepository.getTripById(tripId) }
            val reviewsDeferred = async { reviewRepository.getReviewsByTripId(tripId) }

            val tripResult = tripDeferred.await()
            val reviewsResult = reviewsDeferred.await()

            tripResult.fold(
                onSuccess = { trip ->
                    val reviews = reviewsResult.getOrDefault(emptyList())
                    _uiState.value = UiState.Success(TripDetailData(trip, reviews))
                },
                onFailure = {
                    _uiState.value = UiState.Error("impossibile caricare i dettagli del viaggio")
                }
            )
        }
    }
}