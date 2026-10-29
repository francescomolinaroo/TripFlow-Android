package com.tripflow.feature.catalog.presentation.organizer

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.model.UiState
import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.NetworkErrorParser
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import com.tripflow.feature.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class OrganizerTripsViewModel(
    private val catalogRepository: CatalogRepository = CatalogRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TripResponseDTO>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TripResponseDTO>>> = _uiState.asStateFlow()

    private var currentOrganizerId: String? = null

    init {
        loadTrips()
    }

    fun loadTrips() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val token = ApiClient.tokenProvider?.invoke()
            val jwtSub = extractSub(token)
            val me = authRepository.getMe().getOrNull()
            val organizerId = jwtSub ?: me?.id

            currentOrganizerId = organizerId

            if (organizerId.isNullOrBlank()) {
                _uiState.value = UiState.Error("Utente non autenticato o sessione non valida")
                return@launch
            }

            // Tentiamo prima l'endpoint specifico per organizzatore
            val organizerResult = catalogRepository.getTripsByOrganizer(organizerId)

            if (organizerResult.isSuccess) {
                val trips = organizerResult.getOrThrow()
                if (trips.isEmpty()) {
                    // Fallback: cerca tutti i viaggi e filtra per l'organizzatore corrente
                    val fallbackResult = catalogRepository.searchTrips()
                    val fallbackTrips = fallbackResult.getOrNull()?.filter {
                        it.organizerId == organizerId || (me != null && it.organizerId == me.id)
                    } ?: emptyList()

                    if (fallbackTrips.isEmpty()) {
                        _uiState.value = UiState.Empty("Non hai ancora pubblicato nessun viaggio.")
                    } else {
                        _uiState.value = UiState.Success(fallbackTrips)
                    }
                } else {
                    _uiState.value = UiState.Success(trips)
                }
            } else {
                // Se l'endpoint dà errore (es. formato id differente), prova il fallback con search
                val fallbackResult = catalogRepository.searchTrips()
                fallbackResult.fold(
                    onSuccess = { allTrips ->
                        val myTrips = allTrips.filter {
                            it.organizerId == organizerId || (me != null && it.organizerId == me.id)
                        }
                        if (myTrips.isEmpty()) {
                            _uiState.value = UiState.Empty("Non hai ancora pubblicato nessun viaggio.")
                        } else {
                            _uiState.value = UiState.Success(myTrips)
                        }
                    },
                    onFailure = { err ->
                        _uiState.value = UiState.Error(err.message ?: "Impossibile caricare i tuoi viaggi")
                    }
                )
            }
        }
    }

    fun deleteTrip(tripId: String, onDeleted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            catalogRepository.deleteTrip(tripId).fold(
                onSuccess = {
                    loadTrips()
                    onDeleted()
                },
                onFailure = { e ->
                    val msg = NetworkErrorParser.getUserMessage(e, defaultFallback = "Impossibile eliminare il viaggio")
                    onError(msg)
                }
            )
        }
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

