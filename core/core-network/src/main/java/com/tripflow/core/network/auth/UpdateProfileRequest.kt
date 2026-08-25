package com.tripflow.core.network.auth

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String? = null,
    val phoneNumber: String? = null,
    val profileImage: String? = null
)