package com.tripflow.core.auth

import android.content.Context

class TokenStorage(context: Context) {
    private val preferences = context.getSharedPreferences("tripflow_auth", Context.MODE_PRIVATE)

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