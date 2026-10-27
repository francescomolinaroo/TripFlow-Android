package com.tripflow.feature.itinerary.session

import com.tripflow.core.auth.TokenStorage
import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.auth.AuthApi
import com.tripflow.core.network.auth.RefreshTokenRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Rinnova l'access token quando il backend risponde 401 (token Keycloak scaduto,
// di default dopo 5 minuti). Stessa logica di AuthRepository, che pero' la tiene privata.
interface TokenRefresher {
    // Token attualmente salvato, da passare a refreshAfter se la chiamata fallisce.
    fun currentToken(): String?

    // true se dopo la chiamata e' disponibile un token nuovo con cui riprovare.
    suspend fun refreshAfter(failedToken: String?): Boolean

    companion object {
        // Nessun refresh: usato da test e Preview.
        val None = object : TokenRefresher {
            override fun currentToken(): String? = null
            override suspend fun refreshAfter(failedToken: String?): Boolean = false
        }
    }
}

class KeycloakTokenRefresher(
    private val tokenStorage: TokenStorage,
) : TokenRefresher {

    // Client dedicato SENZA l'interceptor di ApiClient: quell'interceptor allega il token
    // salvato (ormai scaduto) e Spring Security risponde 401 anche su /api/auth/refresh,
    // che e' pubblico, se riceve un Bearer non valido.
    private val refreshApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(ApiClient.retrofit.baseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    override fun currentToken(): String? = tokenStorage.getAccessToken()

    // Serializzato: piu' chiamate che ricevono 401 insieme fanno un solo refresh,
    // le altre trovano il token gia' cambiato e riprovano con quello.
    override suspend fun refreshAfter(failedToken: String?): Boolean = mutex.withLock {
        val current = tokenStorage.getAccessToken() ?: return@withLock false
        if (failedToken != null && current != failedToken) return@withLock true
        val refreshToken = tokenStorage.getRefreshToken() ?: return@withLock false
        try {
            val response = refreshApi.refresh(RefreshTokenRequest(refreshToken = refreshToken))
            tokenStorage.save(response.accessToken, response.refreshToken)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    private companion object {
        val mutex = Mutex()
    }
}
