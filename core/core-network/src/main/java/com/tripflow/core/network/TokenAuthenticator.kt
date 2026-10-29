package com.tripflow.core.network

import com.tripflow.core.network.auth.AuthApi
import com.tripflow.core.network.auth.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

// OkHttp chiama questo quando il backend risponde 401, cioe' quando l'access token Keycloak e'
// scaduto (di default dopo 5 minuti). Qui rinnoviamo il token e rilanciamo la stessa richiesta con
// quello nuovo. Stando nell'OkHttpClient di ApiClient vale per tutti i moduli (booking, review,
// catalog, itinerary), non solo per gli itinerari come prima.
class TokenAuthenticator(
    private val storeProvider: () -> SessionTokenStore?,
    private val refreshApi: AuthApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val store = storeProvider() ?: return null

        // Abbiamo gia' riprovato una volta con un token fresco e il server risponde ancora 401:
        // insistere non ha senso, lasciamo passare il 401 cosi' la UI manda l'utente al login.
        if (priorResponseCount(response) >= 1) return null

        // Il Bearer (ormai scaduto) che era stato messo sulla richiesta finita in 401.
        val failedToken = response.request().header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        // Se piu' chiamate falliscono insieme solo la prima rinnova davvero: le altre entrano qui
        // quando il token e' gia' cambiato e si limitano a riprovare con quello.
        val newToken = synchronized(lock) {
            val current = store.accessToken()
            if (current != null && !current.isBlank() && current != failedToken) {
                current
            } else {
                refresh(store, failedToken)
            }
        } ?: return null

        return response.request().newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    // Ritorna il nuovo access token, oppure null se la sessione e' da chiudere.
    private fun refresh(store: SessionTokenStore, failedToken: String?): String? {
        val current = store.accessToken() ?: return null
        // Qualcun altro ha gia' rinnovato mentre aspettavamo il lock: usiamo il token nuovo.
        if (failedToken != null && current != failedToken) return current

        val refreshToken = store.refreshToken() ?: return null
        return try {
            // .execute() e' bloccante ma qui va bene: l'Authenticator gira gia' su un thread di OkHttp.
            val result = refreshApi.refreshBlocking(RefreshTokenRequest(refreshToken = refreshToken)).execute()
            if (result.isSuccessful) {
                val body = result.body() ?: return null
                store.save(body.accessToken, body.refreshToken)
                body.accessToken
            } else {
                // 400/401 vuol dire che pure il refresh token non vale piu': la sessione e' finita.
                // Altri codici (429, 5xx, rete) sono temporanei e non devono sloggare l'utente.
                if (result.code() == 400 || result.code() == 401) store.clear()
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Quante volte questa richiesta era gia' stata ritentata prima di arrivare qui.
    private fun priorResponseCount(response: Response): Int {
        var count = 0
        var prior = response.priorResponse()
        while (prior != null) {
            count++
            prior = prior.priorResponse()
        }
        return count
    }

    private companion object {
        val lock = Any()
    }
}
