package com.tripflow.core.network.auth

data class PublicUserResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val profileImage: String? = null
)
