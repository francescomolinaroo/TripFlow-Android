package com.tripflow.feature.catalog.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import com.tripflow.feature.catalog.domain.repository.CatalogRepository
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
    private val repository: CatalogRepository = CatalogRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TripResponseDTO>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TripResponseDTO>>> = _uiState.asStateFlow()

    private val _filters = MutableStateFlow(DiscoverFilters())
    val filters: StateFlow<DiscoverFilters> = _filters.asStateFlow()

    init {
        loadTrips()
    }

    fun loadTrips() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
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
                        _uiState.value = UiState.Success(trips)
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