package com.tripflow.core.network.catalog.dto

data class ActivityResponseDTO(
    val id: String,
    val name: String,
    val description: String?,
    val duration: Int,
    val price: Double,
    val availableSpots: Int,
    val tripId: String
)