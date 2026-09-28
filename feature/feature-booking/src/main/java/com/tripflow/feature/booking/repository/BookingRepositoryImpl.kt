package com.tripflow.feature.booking.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.booking.api.BookingApi
import com.tripflow.feature.booking.model.PagamentoIntentResponse
import com.tripflow.feature.booking.model.PagamentoResponse
import com.tripflow.feature.booking.model.PrenotazioneAttivitaRequest
import com.tripflow.feature.booking.model.PrenotazioneRequest
import com.tripflow.feature.booking.model.PrenotazioneResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookingRepositoryImpl(
    private val api: BookingApi = defaultApi()
) : BookingRepository {

    override suspend fun creaPrenotazione(request: PrenotazioneRequest): UiState<PrenotazioneResponse> =
        call { api.crea(request) }

    override suspend fun trovaPrenotazione(id: String): UiState<PrenotazioneResponse> =
        call { api.trova(id) }

    override suspend fun miePrenotazioni(): UiState<List<PrenotazioneResponse>> =
        call { api.mie() }

    override suspend fun miePrenotazioniAttive(): UiState<List<PrenotazioneResponse>> =
        call { api.mieAttive() }

    override suspend fun annullaPrenotazione(id: String): UiState<PrenotazioneResponse> =
        call { api.annulla(id) }

    override suspend fun aggiungiAttivita(prenotazioneId: String, attivitaId: String): UiState<PrenotazioneResponse> =
        call { api.aggiungiAttivita(prenotazioneId, PrenotazioneAttivitaRequest(attivitaId)) }

    override suspend fun rimuoviAttivita(prenotazioneId: String, attivitaId: String): UiState<PrenotazioneResponse> =
        call { api.rimuoviAttivita(prenotazioneId, attivitaId) }

    override suspend fun avviaPagamento(prenotazioneId: String): UiState<PagamentoIntentResponse> =
        call { api.avviaPagamento(prenotazioneId) }

    override suspend fun trovaPagamento(prenotazioneId: String): UiState<PagamentoResponse> =
        call { api.trovaPagamento(prenotazioneId) }

    private suspend fun <T> call(block: suspend () -> T): UiState<T> = withContext(Dispatchers.IO) {
        try {
            UiState.Success(block())
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun <T> handleError(e: Exception): UiState<T> {
        return when (e) {
            is HttpException -> {
                val code = e.code()
                val serverMessage = e.response()?.errorBody()?.string()
                    ?.let { body -> runCatching { JSONObject(body).optString("message") }.getOrNull() }
                    ?.takeIf { it.isNotBlank() }
                val message = when (code) {
                    401 -> "Sessione scaduta, effettua di nuovo l'accesso"
                    403 -> "Non hai i permessi per questa operazione"
                    404 -> serverMessage ?: "Prenotazione non trovata"
                    400, 409 -> serverMessage ?: "Operazione non consentita"
                    else -> "Errore del server ($code)"
                }
                val nonRetryableCodes = setOf(400, 401, 403, 404, 409)
                UiState.Error(message, retryable = code !in nonRetryableCodes)
            }
            else -> UiState.Error(e.message ?: "Errore di rete", retryable = true)
        }
    }

    private companion object {
        fun defaultApi(): BookingApi = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") //da rivedere
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookingApi::class.java)
    }
}
