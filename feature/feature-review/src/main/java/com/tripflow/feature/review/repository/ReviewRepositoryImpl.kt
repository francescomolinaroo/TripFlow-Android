package com.tripflow.feature.review.repository

import com.tripflow.core.model.UiState
import com.tripflow.core.network.ApiClient
import com.tripflow.feature.review.api.ReviewApi
import com.tripflow.feature.review.model.RecensioneRequest
import com.tripflow.feature.review.model.RecensioneResponse
import com.tripflow.feature.review.model.RecensioneUpdateRequest
import com.tripflow.feature.review.model.TipoOggetto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReviewRepositoryImpl(
    private val api: ReviewApi = defaultApi()
) : ReviewRepository {

    override suspend fun creaRecensione(request: RecensioneRequest): UiState<RecensioneResponse> =
        call { api.crea(request) }

    override suspend fun mieRecensioni(): UiState<List<RecensioneResponse>> =
        call { api.mie() }

    override suspend fun modificaRecensione(id: String, request: RecensioneUpdateRequest): UiState<RecensioneResponse> =
        call { api.modifica(id, request) }

    override suspend fun eliminaRecensione(id: String): UiState<Unit> =
        call { api.elimina(id) }

    override suspend fun puoRecensire(
        prenotazioneId: String,
        tipoOggetto: TipoOggetto,
        oggettoId: String
    ): UiState<Boolean> =
        call { api.puoRecensire(prenotazioneId, tipoOggetto, oggettoId) }

    override suspend fun trovaRecensione(id: String): UiState<RecensioneResponse> =
        call { api.trova(id) }

    override suspend fun recensioniOggetto(oggettoId: String): UiState<List<RecensioneResponse>> =
        call { api.perOggetto(oggettoId) }

    override suspend fun mediaValutazione(oggettoId: String): UiState<Double> =
        call { api.media(oggettoId) }

    override suspend fun contaRecensioni(oggettoId: String): UiState<Long> =
        call { api.conta(oggettoId) }

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
                    404 -> serverMessage ?: "Recensione non trovata"
                    400, 409 -> serverMessage ?: "Operazione non consentita"
                    503 -> serverMessage ?: "Servizio momentaneamente non disponibile, riprova più tardi"
                    else -> "Errore del server ($code)"
                }
                val nonRetryableCodes = setOf(400, 401, 403, 404, 409)
                UiState.Error(message, retryable = code !in nonRetryableCodes)
            }
            else -> UiState.Error(e.message ?: "Errore di rete", retryable = true)
        }
    }

    private companion object {
        fun defaultApi(): ReviewApi = ApiClient.create(ReviewApi::class.java)
    }
}
