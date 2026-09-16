package com.tripflow.core.network.catalog.dto

data class TripResponseDTO(
    val id: String,
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val price: Double,
    val availableSpots: Int,
    val organizerId: String,
    val description: String?,
    val images: List<String>?,
    val rating: Double?,
    val reviewCount: Int?,
    val activities: List<ActivityResponseDTO>?
)