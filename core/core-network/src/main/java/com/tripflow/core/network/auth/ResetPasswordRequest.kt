package com.tripflow.core.network.auth

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)
