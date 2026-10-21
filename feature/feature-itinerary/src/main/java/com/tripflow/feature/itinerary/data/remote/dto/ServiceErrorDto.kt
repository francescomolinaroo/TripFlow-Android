package com.tripflow.feature.itinerary.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ServiceErrorDto(
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("message") val message: String? = null
)
