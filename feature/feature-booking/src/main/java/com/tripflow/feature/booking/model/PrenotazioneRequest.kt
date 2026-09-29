package com.tripflow.feature.booking.model

data class PrenotazioneRequest(
    val viaggioId: String,
    val numeroPartecipanti: Int,
    val note: String?,
    val attivitaIds: List<String>
)
