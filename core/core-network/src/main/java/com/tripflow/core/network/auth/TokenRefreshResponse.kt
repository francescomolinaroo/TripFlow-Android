package com.tripflow.core.network.auth

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String
)
