package com.tripflow.feature.itinerary.model

data class MyItinerary(
    val id: String,
    val title: String,
    val dateRange: String,
    val stopsCount: Int,
    val visibility: Visibility,
    val stopsPreview: List<StopPreview>
)

data class StopPreview(
    val title: String,
    val time: String
)
