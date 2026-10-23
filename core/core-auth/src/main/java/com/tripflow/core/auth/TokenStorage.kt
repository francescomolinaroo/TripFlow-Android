package com.tripflow.core.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.tripflow.core.network.ApiClient

class TokenStorage(context: Context) {
    private val preferences = EncryptedSharedPreferences.create(
        "tripflow_auth_secure",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    init {
        ApiClient.tokenProvider = { getAccessToken() }
    }

    fun save(accessToken: String, refreshToken: String?) {
        preferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = preferences.getString("access_token", null)

    fun getRefreshToken(): String? = preferences.getString("refresh_token", null)

    fun clear() {
        preferences.edit().clear().apply()
    }
}
