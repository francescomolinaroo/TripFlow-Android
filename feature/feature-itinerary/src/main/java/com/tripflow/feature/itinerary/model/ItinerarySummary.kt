package com.tripflow.feature.itinerary.model

import java.time.LocalDate
import java.util.UUID

data class ItinerarySummary(
    val id: UUID,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isPublic: Boolean,
    val stagesCount: Int,
    val previewStages: List<StagePreview>,
    val createdAt: String,
    val updatedAt: String
)

data class StagePreview(
    val id: UUID,
    val dayNumber: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val isFromCatalog: Boolean
)