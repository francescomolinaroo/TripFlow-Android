package com.tripflow.core.network.auth

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
