package com.tripflow.feature.itinerary.model

data class CreateItineraryRequest(
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val isPublic: Boolean
)

data class UpdateVisibilityRequest(
    val isPublic: Boolean
)