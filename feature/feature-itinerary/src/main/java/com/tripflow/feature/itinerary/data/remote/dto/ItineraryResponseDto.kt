package com.tripflow.feature.itinerary.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class ItinerarySummaryResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("stagesCount") val stagesCount: Int,
    @SerializedName("previewStages") val previewStages: List<StagePreviewResponseDto>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class StagePreviewResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("dayNumber") val dayNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("isFromCatalog") val isFromCatalog: Boolean
)

data class ItineraryDetailResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("stages") val stages: List<StageDetailResponseDto>,
    @SerializedName("totalStages") val totalStages: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class StageDetailResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("dayNumber") val dayNumber: Int,
    @SerializedName("date") val date: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("isFromCatalog") val isFromCatalog: Boolean,
    @SerializedName("catalogItemId") val catalogItemId: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("orderIndex") val orderIndex: Int
)

data class CatalogItemResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("location") val location: String,
    @SerializedName("category") val category: String,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("averageRating") val averageRating: Double?
)

data class CatalogSearchResponseDto(
    @SerializedName("content") val content: List<CatalogItemResponseDto>,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("currentPage") val currentPage: Int
)