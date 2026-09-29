package com.tripflow.feature.booking.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.booking.model.PagamentoIntentResponse
import com.tripflow.feature.booking.model.PagamentoResponse
import com.tripflow.feature.booking.model.PrenotazioneRequest
import com.tripflow.feature.booking.model.PrenotazioneResponse

interface BookingRepository {

    suspend fun creaPrenotazione(request: PrenotazioneRequest): UiState<PrenotazioneResponse>

    suspend fun trovaPrenotazione(id: String): UiState<PrenotazioneResponse>

    suspend fun miePrenotazioni(): UiState<List<PrenotazioneResponse>>

    suspend fun miePrenotazioniAttive(): UiState<List<PrenotazioneResponse>>

    suspend fun annullaPrenotazione(id: String): UiState<PrenotazioneResponse>

    suspend fun aggiungiAttivita(prenotazioneId: String, attivitaId: String): UiState<PrenotazioneResponse>

    suspend fun rimuoviAttivita(prenotazioneId: String, attivitaId: String): UiState<PrenotazioneResponse>

    suspend fun avviaPagamento(prenotazioneId: String): UiState<PagamentoIntentResponse>

    suspend fun trovaPagamento(prenotazioneId: String): UiState<PagamentoResponse>
}
