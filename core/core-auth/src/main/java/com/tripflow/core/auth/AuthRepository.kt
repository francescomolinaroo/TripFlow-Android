package com.tripflow.core.auth

import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.auth.RegisterRequest
import com.tripflow.core.network.auth.RegisterResponse

class AuthRepository {
    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            Result.success(ApiClient.authApi.register(request))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
