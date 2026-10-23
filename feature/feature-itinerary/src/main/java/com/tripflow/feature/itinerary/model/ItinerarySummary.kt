package com.tripflow.feature.itinerary.model

import java.time.LocalDate

data class ItinerarySummary(
    val id: String,
    val ownerId: String?,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val visibility: Visibility,
    val canEdit: Boolean?,
    val stagesCount: Int,
    val previewStages: List<StagePreview>,
    val createdAt: String,
    val updatedAt: String
)

data class StagePreview(
    val id: String,
    val dayNumber: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val stopType: StopType
) {
    val isFromCatalog: Boolean
        get() = stopType != StopType.CUSTOM
}
