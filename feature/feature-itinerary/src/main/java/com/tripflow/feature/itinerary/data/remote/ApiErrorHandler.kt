package com.tripflow.feature.itinerary.data.remote

import com.google.gson.Gson
import com.tripflow.feature.itinerary.data.remote.dto.ServiceErrorDto
import retrofit2.HttpException
import java.io.IOException

data class ApiError(
    val message: String,
    val retryable: Boolean
)

object ApiErrorHandler {

    private val gson = Gson()

    fun from(throwable: Throwable): ApiError {
        return when (throwable) {
            is HttpException -> fromHttpException(throwable)
            is IOException -> ApiError(
                message = "Errore di rete, verifica la connessione",
                retryable = true
            )
            else -> ApiError(
                message = throwable.message ?: "Errore imprevisto",
                retryable = true
            )
        }
    }

    private fun fromHttpException(exception: HttpException): ApiError {
        val code = exception.code()
        val message = readServiceMessage(exception) ?: fallbackMessage(code)
        val retryable = code == 408 || code == 429 || code >= 500
        return ApiError(message = message, retryable = retryable)
    }

    private fun readServiceMessage(exception: HttpException): String? {
        return runCatching {
            val body = exception.response()?.errorBody()?.string()
            if (body.isNullOrBlank()) {
                null
            } else {
                gson.fromJson(body, ServiceErrorDto::class.java)
                    ?.message
                    ?.takeIf { it.isNotBlank() }
            }
        }.getOrNull()
    }

    private fun fallbackMessage(code: Int): String = when (code) {
        400 -> "Richiesta non valida: controlla i dati inseriti"
        401 -> "Sessione scaduta, effettua di nuovo l'accesso"
        403 -> "Non hai i permessi per questa operazione"
        404 -> "Risorsa non trovata"
        405 -> "Operazione non consentita su questa risorsa"
        409 -> "Conflitto: la risorsa esiste già"
        408 -> "La richiesta ha impiegato troppo tempo, riprova"
        429 -> "Troppe richieste, attendi qualche secondo e riprova"
        500 -> "Errore interno del server"
        503 -> "Servizio temporaneamente non disponibile"
        else -> "Errore del server ($code)"
    }
}
