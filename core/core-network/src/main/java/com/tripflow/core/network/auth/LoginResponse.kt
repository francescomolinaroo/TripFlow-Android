package com.tripflow.core.network.auth

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String,
    val user: UserResponse?
)
