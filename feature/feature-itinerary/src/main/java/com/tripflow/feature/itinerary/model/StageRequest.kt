package com.tripflow.feature.itinerary.model

import java.time.LocalDate
import java.util.UUID

sealed interface StageSource {
    data class FromCatalog(
        val catalogItemId: UUID,
        val stopType: StopType = StopType.TRIP,
        // Nome del viaggio/attivita' scelto: salvato come titolo della tappa, cosi'
        // dettaglio e lista mostrano "Visita Uffizi" e non un testo generico.
        val title: String? = null
    ) : StageSource {
        init {
            require(stopType != StopType.CUSTOM) {
                "Le tappe da catalogo devono essere TRIP o ACTIVITY"
            }
        }
    }

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
    val notes: String?,
    val orderIndex: Int? = null
)

data class UpdateStageRequest(
    val date: LocalDate? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val title: String? = null,
    val description: String? = null,
    val location: String? = null,
    val stopType: StopType? = null,
    val viaggioId: String? = null,
    val attivitaId: String? = null,
    val notes: String? = null,
    val orderIndex: Int? = null
)
