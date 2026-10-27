package com.tripflow.feature.itinerary.model

import java.time.LocalDate

data class ItineraryDetail(
    val id: String,
    val ownerId: String?,
    val title: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val visibility: Visibility,
    val canEdit: Boolean?,
    val stages: List<StageDetail>,
    val totalStages: Int,
    val createdAt: String,
    val updatedAt: String,
    // Valorizzato solo per gli itinerari PUBLIC: identifica il link pubblico.
    val publicToken: String? = null
)

data class StageDetail(
    val id: String,
    val dayNumber: Int,
    val date: LocalDate,
    val startTime: String,
    val endTime: String,
    val title: String,
    val description: String?,
    val location: String?,
    val stopType: StopType,
    val viaggioId: String?,
    val attivitaId: String?,
    val notes: String?,
    val orderIndex: Int
) {
    val isFromCatalog: Boolean
        get() = stopType != StopType.CUSTOM

    val catalogItemId: String?
        get() = viaggioId ?: attivitaId
}
