package com.tripflow.feature.catalog.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import com.tripflow.feature.catalog.data.repository.ReviewRepositoryImpl
import com.tripflow.feature.catalog.domain.repository.CatalogRepository
import com.tripflow.feature.catalog.domain.repository.ReviewRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoverFilters(
    val query: String = "",
    val showOnlyAvailable: Boolean = false,
    val maxPrice: Double? = null,
    val targetMonth: Int? = null
)

class DiscoverViewModel(
    private val repository: CatalogRepository = CatalogRepositoryImpl(),
    private val reviewRepository: ReviewRepository = ReviewRepositoryImpl(),
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TripResponseDTO>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TripResponseDTO>>> = _uiState.asStateFlow()

    private val _filters = MutableStateFlow(DiscoverFilters())
    val filters: StateFlow<DiscoverFilters> = _filters.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userImageUrl = MutableStateFlow<String?>(null)
    val userImageUrl: StateFlow<String?> = _userImageUrl.asStateFlow()

    private var loadTripsJob: Job? = null

    init {
        loadTrips()
    }

    // Chiamata a ogni ingresso nella schermata: nome e avatar nell'intestazione restano
    // aggiornati anche dopo una modifica del profilo.
    fun loadUser() {
        if (authRepository == null) return
        viewModelScope.launch {
            authRepository.getMe().onSuccess { user ->
                _userName.value = "${user.firstName} ${user.lastName}"
                _userImageUrl.value = user.profileImage
            }
        }
    }

    fun loadTrips() {
        _uiState.value = UiState.Loading
        loadTripsJob?.cancel()
        loadTripsJob = viewModelScope.launch {
            val currentFilters = _filters.value

            val result = repository.searchTrips(
                destination = currentFilters.query,
                minAvailableSpots = if (currentFilters.showOnlyAvailable) 1 else null,
                maxPrice = currentFilters.maxPrice
            )

            result.fold(
                onSuccess = { trips ->
                    if (trips.isEmpty()) {
                        _uiState.value = UiState.Empty("Nessun viaggio in partenza trovato.")
                    } else {
                        // Chiama /media dal FE per ogni card per popolare il rating del viaggio
                        val tripsWithRatings = coroutineScope {
                            trips.map { trip ->
                                async {
                                    val currentRating = trip.rating
                                    if (currentRating != null && currentRating > 0.0) {
                                        trip
                                    } else {
                                        val media = reviewRepository.getAverageRating(trip.id).getOrNull()
                                        if (media != null && media > 0.0) {
                                            trip.copy(rating = media)
                                        } else {
                                            trip
                                        }
                                    }
                                }
                            }.awaitAll()
                        }
                        _uiState.value = UiState.Success(tripsWithRatings)
                    }
                },
                onFailure = {
                    _uiState.value = UiState.Error("Impossibile caricare i viaggi. Riprova.")
                }
            )
        }
    }

    fun updateQuery(newQuery: String) {
        _filters.update { it.copy(query = newQuery) }
        loadTrips()
    }

    fun toggleAvailableSpots() {
        _filters.update { it.copy(showOnlyAvailable = !it.showOnlyAvailable) }
        loadTrips()
    }

    fun toggleMaxPrice(price: Double) {
        _filters.update {
            it.copy(maxPrice = if (it.maxPrice == price) null else price)
        }
        loadTrips()
    }

    fun toggleMonth(month: Int) {
        _filters.update {
            it.copy(targetMonth = if (it.targetMonth == month) null else month)
        }
        loadTrips()
    }
}