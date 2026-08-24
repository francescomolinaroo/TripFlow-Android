package com.tripflow.feature.itinerary.model

import java.time.LocalDate
import java.util.UUID

sealed interface StageSource {
    data class FromCatalog(val catalogItemId: UUID) : StageSource
    data class Custom(
        val title: String,
        val description: String?,
        val location: String?
    ) : StageSource
}

data class CreateStageRequest(
    val dayNumber: Int,
    val date: LocalDate,
    val startTime: String,
    val endTime: String,
    val source: StageSource,
    val notes: String?
)

data class UpdateStageRequest(
    val startTime: String? = null,
    val endTime: String? = null,
    val title: String? = null,
    val description: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val orderIndex: Int? = null
)