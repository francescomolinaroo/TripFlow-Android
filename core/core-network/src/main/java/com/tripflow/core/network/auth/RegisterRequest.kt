package com.tripflow.core.network.auth

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String = "TRAVELER",
    val dateOfBirth: String? = null,
    val phoneNumber: String? = null,
    val profileImage: String? = null
)
