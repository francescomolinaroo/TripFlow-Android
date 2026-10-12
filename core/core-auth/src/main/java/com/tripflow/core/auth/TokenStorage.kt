package com.tripflow.core.auth

import android.content.Context
import com.tripflow.core.network.ApiClient

class TokenStorage(context: Context) {
    private val preferences = context.getSharedPreferences("tripflow_auth", Context.MODE_PRIVATE)

    init {
        // Collega automaticamente il token all'interceptor di rete
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