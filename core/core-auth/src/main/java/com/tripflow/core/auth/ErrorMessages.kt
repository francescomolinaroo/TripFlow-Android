package com.tripflow.core.auth

import retrofit2.HttpException
import java.io.IOException

// Messaggio da mostrare all'utente al posto di quello tecnico dell'eccezione (es. "HTTP 400 ").
fun Throwable.userMessage(fallback: String): String = when (this) {
    is HttpException -> when (code()) {
        400 -> "Dati non validi: controlla i campi inseriti"
        401 -> "Sessione scaduta: accedi di nuovo"
        413 -> "Immagine troppo grande (massimo 5 MB)"
        415 -> "Formato non supportato: usa JPEG, PNG o WebP"
        429 -> "Troppe richieste: riprova tra un minuto"
        in 500..599 -> "Servizio non disponibile, riprova più tardi"
        else -> fallback
    }
    is IOException -> "Connessione assente: controlla la rete e riprova"
    else -> fallback
}
