package com.tripflow.feature.itinerary.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.MyItinerary
import com.tripflow.feature.itinerary.model.StopPreview
import java.util.UUID

interface MyItineraryRepository {
    suspend fun getMyItineraries(): UiState<List<MyItinerary>>
}

class FakeMyItineraryRepository : MyItineraryRepository {

    private val mockItineraries = listOf(
        MyItinerary(
            id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            title = "Amalfi in 5 giorni",
            dateRange = "14 – 18 set 2026",
            stopsCount = 6,
            isPublic = false,
            stopsPreview = listOf(
                StopPreview(title = "Tour in barca a Capri", time = "09:00"),
                StopPreview(title = "Pranzo da Nonna Rosa", time = "13:30")
            )
        ),
        MyItinerary(
            id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            title = "Weekend a Bolzano",
            dateRange = "22 – 24 set 2026",
            stopsCount = 3,
            isPublic = true,
            stopsPreview = listOf(
                StopPreview(title = "Arrivo e giro in centro", time = "16:00")
            )
        )
    )

    override suspend fun getMyItineraries(): UiState<List<MyItinerary>> {
        return UiState.Success(mockItineraries)
    }
}