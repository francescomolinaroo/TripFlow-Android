package com.tripflow.core.network.auth

data class UserResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val dateOfBirth: String?,
    val phoneNumber: String?,
    val profileImage: String?
)