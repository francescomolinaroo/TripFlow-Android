package com.tripflow.core.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.SessionTokenStore

class TokenStorage(context: Context) : SessionTokenStore {
    private val preferences = EncryptedSharedPreferences.create(
        "tripflow_auth_secure",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    init {
        ApiClient.tokenProvider = { getAccessToken() }
        // Da' all'Authenticator di core-network accesso ai token per il refresh automatico sul 401.
        ApiClient.sessionTokenStore = this
    }

    override fun save(accessToken: String, refreshToken: String?) {
        preferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = preferences.getString("access_token", null)

    fun getRefreshToken(): String? = preferences.getString("refresh_token", null)

    override fun clear() {
        preferences.edit().clear().apply()
    }

    // Metodi dell'interfaccia SessionTokenStore: delegano ai getter gia' esistenti.
    override fun accessToken(): String? = getAccessToken()

    override fun refreshToken(): String? = getRefreshToken()
}
