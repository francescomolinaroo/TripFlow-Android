package com.tripflow.feature.itinerary.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ItineraryResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("ownerId") val ownerId: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("visibility") val visibility: String? = null,
    @SerializedName("canEdit") val canEdit: Boolean? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("stops") val stops: List<StopResponseDto> = emptyList()
)

data class StopResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("stopOrder") val stopOrder: Int? = null,
    @SerializedName("stopType") val stopType: String? = null,
    @SerializedName("viaggioId") val viaggioId: String? = null,
    @SerializedName("attivitaId") val attivitaId: String? = null,
    @SerializedName("customTitle") val customTitle: String? = null,
    @SerializedName("customDescription") val customDescription: String? = null,
    @SerializedName("startDatetime") val startDatetime: String? = null,
    @SerializedName("endDatetime") val endDatetime: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class TripResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("destination") val destination: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("images") val images: List<String>? = null
)
