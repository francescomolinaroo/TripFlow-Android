package com.tripflow.core.auth

import com.tripflow.core.network.NetworkErrorParser
import retrofit2.HttpException
import java.io.IOException

// Messaggio da mostrare all'utente al posto di quello tecnico dell'eccezione (es. "HTTP 400 ").
fun Throwable.userMessage(fallback: String): String = when (this) {
    is HttpException -> {
        val serverMessage = NetworkErrorParser.parseServerMessage(this)
        when (code()) {
            400 -> serverMessage ?: "Dati non validi: controlla i campi inseriti"
            401 -> "Sessione scaduta: accedi di nuovo"
            403 -> serverMessage ?: "Non hai i permessi per questa operazione"
            404 -> serverMessage ?: fallback
            409 -> serverMessage ?: "Operazione non consentita: conflitto con dati esistenti"
            413 -> "Immagine troppo grande (massimo 5 MB)"
            415 -> "Formato non supportato: usa JPEG, PNG o WebP"
            429 -> "Troppe richieste: riprova tra un minuto"
            in 500..599 -> "Servizio non disponibile, riprova più tardi"
            else -> serverMessage ?: fallback
        }
    }
    is IOException -> "Connessione assente: controlla la rete e riprova"
    else -> NetworkErrorParser.parseServerMessage(this) ?: (message ?: fallback)
}
