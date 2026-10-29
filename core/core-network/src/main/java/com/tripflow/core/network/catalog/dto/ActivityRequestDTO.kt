package com.tripflow.core.network.catalog.dto

data class ActivityRequestDTO(
    val name: String,
    val description: String?,
    val duration: Int,       // minuti
    val price: Double,
    val availableSpots: Int,
    val tripId: String
)

