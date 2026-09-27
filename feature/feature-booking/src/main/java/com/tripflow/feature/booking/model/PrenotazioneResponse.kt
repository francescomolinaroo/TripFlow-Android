package com.tripflow.feature.booking.model

import java.math.BigDecimal

data class PrenotazioneResponse(
    val id: String,
    val viaggioId: String,
    val titoloViaggio: String?,
    val destinazione: String?,
    val dataInizio: String?,
    val dataFine: String?,
    val prezzoUnitarioAlMomentoDelBooking: BigDecimal?,
    val numeroPartecipanti: Int,
    val prezzoTotale: BigDecimal,
    val stato: StatoPrenotazione,
    val scadenzaIl: String?,
    val secondiAllaScadenza: Long?,
    val note: String?,
    val attivitaSelezionate: List<PrenotazioneAttivitaResponse>?,
    val infoPagamento: PagamentoResponse?
)
