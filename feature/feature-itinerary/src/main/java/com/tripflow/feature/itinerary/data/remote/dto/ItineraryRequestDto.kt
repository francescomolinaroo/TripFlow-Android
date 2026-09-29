package com.tripflow.feature.itinerary.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class CreateItineraryRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("isPublic") val isPublic: Boolean
)

data class UpdateVisibilityRequestDto(
    @SerializedName("isPublic") val isPublic: Boolean
)

data class CreateStageRequestDto(
    @SerializedName("dayNumber") val dayNumber: Int,
    @SerializedName("date") val date: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("source") val source: StageSourceRequestDto,
    @SerializedName("notes") val notes: String?
)

sealed class StageSourceRequestDto {
    abstract val type: String

    data class FromCatalog(
        @SerializedName("catalogItemId") val catalogItemId: String,
        override val type: String = "FROM_CATALOG"
    ) : StageSourceRequestDto()

    data class Custom(
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String?,
        @SerializedName("location") val location: String?,
        override val type: String = "CUSTOM"
    ) : StageSourceRequestDto()
}

data class UpdateStageRequestDto(
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("orderIndex") val orderIndex: Int?
)