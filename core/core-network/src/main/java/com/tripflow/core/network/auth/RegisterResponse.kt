package com.tripflow.core.network.auth

import java.util.UUID

data class RegisterResponse(
    val id: UUID,
    val email: String,
    val requiresLogin: Boolean
)
