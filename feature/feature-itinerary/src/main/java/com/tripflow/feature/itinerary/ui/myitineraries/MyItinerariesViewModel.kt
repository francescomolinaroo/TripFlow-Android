package com.tripflow.feature.itinerary.ui.myitineraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.MyItinerary
import com.tripflow.feature.itinerary.repository.FakeMyItineraryRepository
import com.tripflow.feature.itinerary.repository.MyItineraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyItinerariesViewModel(
    private val repository: MyItineraryRepository = FakeMyItineraryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<MyItinerary>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadItineraries()
    }

    fun loadItineraries() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = repository.getMyItineraries()
        }
    }

    fun onItineraryDeleted() {
        loadItineraries()
    }

    fun onItineraryUpdated() {
        loadItineraries()
    }
}