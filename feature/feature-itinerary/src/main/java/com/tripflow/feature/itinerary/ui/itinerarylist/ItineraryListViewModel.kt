package com.tripflow.feature.itinerary.ui.itinerarylist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import kotlinx.coroutines.launch

class ItineraryListViewModel(
    private val repository: ItineraryRepository = FakeItineraryRepository()
) : ViewModel() {

    var uiState by mutableStateOf<UiState<List<ItinerarySummary>>>(UiState.Loading)
        private set

    init { loadItineraries() }

    fun loadItineraries() {
        viewModelScope.launch {
            uiState = UiState.Loading
            uiState = repository.getMyItineraries()
        }
    }

    fun onItineraryDeleted() { loadItineraries() }
    fun onItineraryUpdated() { loadItineraries() }
}