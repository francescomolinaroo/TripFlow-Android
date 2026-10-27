package com.tripflow.feature.itinerary.session

import com.google.gson.JsonParser
import com.tripflow.core.auth.TokenStorage
import java.util.Base64

// Fornisce l'id dell'utente autenticato, usato per capire se e' il proprietario
// di un itinerario (solo l'owner puo' cambiarne la visibilita').
fun interface CurrentUserProvider {
    fun currentUserId(): String?

    companion object {
        // Nessun utente noto: usato da Preview e test.
        val None = CurrentUserProvider { null }
    }
}

// Legge il claim "sub" dal JWT salvato: e' lo stesso id che il backend usa come ownerId.
class JwtCurrentUserProvider(
    private val tokenStorage: TokenStorage,
) : CurrentUserProvider {
    override fun currentUserId(): String? = JwtSubject.extract(tokenStorage.getAccessToken())
}

object JwtSubject {
    // Decodifica solo il payload del token (nessuna verifica di firma: serve
    // all'interfaccia, i permessi veri restano controllati dal backend).
    fun extract(token: String?): String? {
        val payload = token?.split('.')?.getOrNull(1) ?: return null
        return try {
            val json = String(Base64.getUrlDecoder().decode(payload), Charsets.UTF_8)
            JsonParser.parseString(json).asJsonObject.get("sub")?.asString
        } catch (e: Exception) {
            null
        }
    }
}
