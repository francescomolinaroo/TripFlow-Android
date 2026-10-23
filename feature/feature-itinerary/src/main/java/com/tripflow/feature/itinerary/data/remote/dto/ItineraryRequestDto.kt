package com.tripflow.feature.itinerary.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateItineraryRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("visibility") val visibility: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String
)

data class UpdateItineraryRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("visibility") val visibility: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String
)

data class UpdateVisibilityRequestDto(
    @SerializedName("visibility") val visibility: String
)

data class AddStopRequestDto(
    @SerializedName("stopOrder") val stopOrder: Int,
    @SerializedName("stopType") val stopType: String,
    @SerializedName("viaggioId") val viaggioId: String?,
    @SerializedName("attivitaId") val attivitaId: String?,
    @SerializedName("customTitle") val customTitle: String?,
    @SerializedName("customDescription") val customDescription: String?,
    @SerializedName("startDatetime") val startDatetime: String,
    @SerializedName("endDatetime") val endDatetime: String,
    @SerializedName("notes") val notes: String?
)

data class UpdateStopRequestDto(
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
