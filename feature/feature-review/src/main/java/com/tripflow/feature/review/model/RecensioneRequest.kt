package com.tripflow.feature.review.model

import java.util.UUID

data class RecensioneRequest(
    val prenotazioneId: String,
    val oggettoId: String,
    val tipoOggetto: TipoOggetto,
    val valutazione: Int,
    val titolo: String?,
    val commento: String?
)
