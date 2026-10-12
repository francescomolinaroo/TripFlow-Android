package com.tripflow.feature.review.model

import java.time.LocalDateTime
import java.util.UUID

data class RecensioneResponse(
    val id: String,
    val viaggiatoreId: String,
    val prenotazioneId: String,
    val tipoOggetto: TipoOggetto,
    val oggettoId: String,
    val oggettoNome: String?,
    val autoreNome: String?,
    val valutazione: Int,
    val titolo: String?,
    val commento: String?,
    val createdAt: String?,
    val updatedAt: String?
)