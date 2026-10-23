package com.tripflow.feature.itinerary.model

data class CreateItineraryRequest(
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val visibility: Visibility
)

data class UpdateVisibilityRequest(
    val visibility: Visibility
)
