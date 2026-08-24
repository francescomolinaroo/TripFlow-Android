package com.tripflow.feature.itinerary.model

import java.time.LocalDate
import java.util.UUID

data class ItineraryDetail(
    val id: UUID,
    val title: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isPublic: Boolean,
    val stages: List<StageDetail>,
    val totalStages: Int,
    val createdAt: String,
    val updatedAt: String
)

data class StageDetail(
    val id: UUID,
    val dayNumber: Int,
    val date: LocalDate,
    val startTime: String,
    val endTime: String,
    val title: String,
    val description: String?,
    val location: String?,
    val isFromCatalog: Boolean,
    val catalogItemId: UUID?,
    val notes: String?,
    val orderIndex: Int
)