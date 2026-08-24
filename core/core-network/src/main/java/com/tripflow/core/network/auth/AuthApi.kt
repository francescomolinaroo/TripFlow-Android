package com.tripflow.core.network.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("user-auth-service/api/auth/register")
    suspend fun register(request: RegisterRequest): RegisterResponse
}
