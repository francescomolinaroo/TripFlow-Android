package com.tripflow.feature.itinerary.model

import java.util.UUID

data class MyItinerary(
    val id: UUID,
    val title: String,
    val dateRange: String,
    val stopsCount: Int,
    val isPublic: Boolean,
    val stopsPreview: List<StopPreview>
)

data class StopPreview(
    val title: String,
    val time: String
)