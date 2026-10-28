package com.tripflow.feature.itinerary.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ShareRequestDto(
    @SerializedName("sharedWithUserId") val sharedWithUserId: String,
    @SerializedName("canEdit") val canEdit: Boolean,
)

data class ShareResponseDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("itineraryId") val itineraryId: Long? = null,
    @SerializedName("sharedWithUserId") val sharedWithUserId: String,
    @SerializedName("canEdit") val canEdit: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
)
