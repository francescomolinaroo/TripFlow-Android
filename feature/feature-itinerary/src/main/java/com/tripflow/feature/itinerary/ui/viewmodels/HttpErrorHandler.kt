package com.tripflow.feature.itinerary.ui.viewmodels

import retrofit2.HttpException

object HttpErrorHandler {

    fun mapHttpError(e: HttpException): HttpError {
        val code = e.code()
        return when (code) {
            400 -> HttpError.ValidationError("Richiesta non valida: ${e.message ?: "dati mancanti o errati"}")
            401 -> HttpError.Unauthorized("Sessione scaduta, effettua di nuovo l'accesso")
            403 -> HttpError.Forbidden("Non hai i permessi per questa operazione")
            404 -> HttpError.NotFound("Risorsa non trovata")
            500 -> HttpError.ServerError("Errore interno del server, riprova più tardi")
            in 500..599 -> HttpError.ServerError("Errore del server ($code), riprova più tardi")
            else -> HttpError.Unknown("Errore HTTP $code: ${e.message ?: "sconosciuto"}")
        }
    }

    fun mapException(e: Exception): HttpError {
        return when (e) {
            is HttpException -> mapHttpError(e)
            else -> HttpError.NetworkError(e.message ?: "Errore di rete, verifica la connessione")
        }
    }

    sealed interface HttpError {
        val message: String

        data class ValidationError(override val message: String) : HttpError
        data class Unauthorized(override val message: String) : HttpError
        data class Forbidden(override val message: String) : HttpError
        data class NotFound(override val message: String) : HttpError
        data class ServerError(override val message: String) : HttpError
        data class NetworkError(override val message: String) : HttpError
        data class Unknown(override val message: String) : HttpError
    }
}