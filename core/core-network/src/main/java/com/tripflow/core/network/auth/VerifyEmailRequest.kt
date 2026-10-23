package com.tripflow.core.network.auth

data class VerifyEmailRequest(
    val email: String,
    val code: String
)
