package com.tripflow.feature.itinerary.ui.itinerarylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import com.tripflow.feature.itinerary.ui.viewmodels.ItineraryListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TODO: Add @HiltViewModel and @Inject when Hilt is configured in the project
// import dagger.hilt.android.lifecycle.HiltViewModel
// import javax.inject.Inject
// @HiltViewModel
class ItineraryListViewModel(
    private val repository: ItineraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItineraryListState.initial)
    val uiState = _uiState.asStateFlow()

    init {
        fetchItineraries()
    }

    fun fetchItineraries() {
        viewModelScope.launch {
            _uiState.value = ItineraryListState(isLoading = true)
            val result = repository.getMyItineraries()
            _uiState.value = mapResultToState(result)
        }
    }

    fun onItineraryDeleted() {
        fetchItineraries()
    }

    fun onItineraryUpdated() {
        fetchItineraries()
    }

    private fun mapResultToState(
        result: com.tripflow.core.model.UiState<List<ItinerarySummary>>
    ): ItineraryListState = when (result) {
        is com.tripflow.core.model.UiState.Loading -> ItineraryListState(isLoading = true)
        is com.tripflow.core.model.UiState.Success -> ItineraryListState(
            isLoading = false,
            itineraries = result.data
        )
        is com.tripflow.core.model.UiState.Empty -> ItineraryListState(
            isLoading = false,
            itineraries = emptyList(),
            errorMessage = result.message
        )
        is com.tripflow.core.model.UiState.Error -> ItineraryListState(
            isLoading = false,
            errorMessage = result.message
        )
    }
}