package com.tripflow.feature.itinerary.ui.viewmodels

import com.tripflow.feature.itinerary.model.ItinerarySummary

data class ItineraryListState(
    val isLoading: Boolean = false,
    val itineraries: List<ItinerarySummary> = emptyList(),
    val errorMessage: String? = null
) {
    companion object {
        val initial = ItineraryListState()
    }
}