package com.tripflow.core.network

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

object NetworkErrorParser {

    /**
     * Tenta di estrarre il messaggio di errore inviato dal server in formato JSON
     * (es. {"message": "..."} o {"error": "..."}) o plain text.
     */
    fun parseServerMessage(throwable: Throwable): String? {
        val httpException = when (throwable) {
            is HttpException -> throwable
            else -> throwable.cause as? HttpException
        } ?: return null

        return runCatching {
            val body = httpException.response()?.errorBody()?.string()?.trim()
            if (body.isNullOrBlank()) return@runCatching null

            if (body.startsWith("{") && body.endsWith("}")) {
                val element = JsonParser.parseString(body)
                if (element.isJsonObject) {
                    val obj = element.asJsonObject
                    val msg = obj.get("message")?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
                        ?: obj.get("error")?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
                        ?: obj.get("detail")?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
                        ?: obj.get("description")?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
                    return@runCatching msg
                }
            }
            if (!body.startsWith("<")) {
                body
            } else {
                null
            }
        }.getOrNull()
    }

    /**
     * Restituisce un messaggio utente leggibile per l'errore specificato, dando
     * priorità al messaggio esplicito restituito dal server in caso di errore 400 (o altri codici).
     */
    fun getUserMessage(
        throwable: Throwable,
        fallback400: String = "Dati non validi: controlla i campi",
        fallback403: String = "Non hai i permessi per questa operazione",
        defaultFallback: String = "Errore di rete, riprova"
    ): String {
        val serverMsg = parseServerMessage(throwable)
        val code = (throwable as? HttpException)?.code()
            ?: (throwable.cause as? HttpException)?.code()
            ?: when {
                throwable.message?.contains("401") == true -> 401
                throwable.message?.contains("403") == true -> 403
                throwable.message?.contains("400") == true -> 400
                else -> null
            }

        return when (code) {
            400 -> serverMsg ?: fallback400
            401 -> "Sessione scaduta, effettua di nuovo il login"
            403 -> serverMsg ?: fallback403
            404 -> serverMsg ?: "Risorsa non trovata"
            409 -> serverMsg ?: "Operazione non consentita: conflitto con dati esistenti"
            413 -> "Immagine troppo grande (massimo 5 MB)"
            415 -> "Formato non supportato: usa JPEG, PNG o WebP"
            429 -> "Troppe richieste: riprova tra un minuto"
            in 500..599 -> "Servizio momentaneamente non disponibile, riprova più tardi"
            else -> {
                if (throwable is IOException) {
                    "Connessione assente: controlla la rete e riprova"
                } else {
                    serverMsg ?: throwable.message ?: defaultFallback
                }
            }
        }
    }
}
