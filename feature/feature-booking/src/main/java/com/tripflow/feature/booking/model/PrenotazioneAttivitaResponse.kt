package com.tripflow.feature.booking.model

import java.math.BigDecimal

data class PrenotazioneAttivitaResponse(
    val id: String,
    val attivitaId: String,
    val nome: String,
    val prezzo: BigDecimal,
    val durataMinuti: Int?,
    val aggiuntoIl: String?
)