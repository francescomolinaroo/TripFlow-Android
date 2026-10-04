package com.tripflow.feature.review.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.review.model.RecensioneRequest
import com.tripflow.feature.review.model.RecensioneResponse
import com.tripflow.feature.review.model.RecensioneUpdateRequest
import com.tripflow.feature.review.model.TipoOggetto

interface ReviewRepository {

    suspend fun creaRecensione(request: RecensioneRequest): UiState<RecensioneResponse>

    suspend fun mieRecensioni(): UiState<List<RecensioneResponse>>

    suspend fun modificaRecensione(id: String, request: RecensioneUpdateRequest): UiState<RecensioneResponse>

    suspend fun eliminaRecensione(id: String): UiState<Unit>

    suspend fun puoRecensire(
        prenotazioneId: String,
        tipoOggetto: TipoOggetto,
        oggettoId: String
    ): UiState<Boolean>

    suspend fun trovaRecensione(id: String): UiState<RecensioneResponse>

    suspend fun recensioniOggetto(oggettoId: String): UiState<List<RecensioneResponse>>

    suspend fun mediaValutazione(oggettoId: String): UiState<Double>

    suspend fun contaRecensioni(oggettoId: String): UiState<Long>
}
