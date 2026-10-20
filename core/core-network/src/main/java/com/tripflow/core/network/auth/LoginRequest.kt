package com.tripflow.core.network.auth

data class LoginRequest(
    val email: String,
    val password: String
)
