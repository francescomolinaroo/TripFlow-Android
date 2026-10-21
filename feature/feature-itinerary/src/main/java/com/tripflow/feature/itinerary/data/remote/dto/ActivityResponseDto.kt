package com.tripflow.feature.itinerary.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ActivityResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("availableSpots") val availableSpots: Int? = null
)
