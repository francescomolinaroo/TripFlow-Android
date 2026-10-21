package com.tripflow.core.network.catalog.dto

data class TripRequestDTO(
    val name: String,
    val destination: String,
    val startDate: String,          // ISO-8601: "yyyy-MM-dd"
    val endDate: String,            // ISO-8601: "yyyy-MM-dd"
    val price: Double,
    val availableSpots: Int,
    val description: String?,
    val images: List<String>?
)
