package com.tripflow.core.network.auth

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {
    @POST("user-auth-service/api/auth/register")
    suspend fun register(request: RegisterRequest): RegisterResponse

    @GET("user-auth-service/api/auth/me")
    suspend fun getMe(@Header("Authorization") authorization: String): UserResponse

    @PUT("user-auth-service/api/auth/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): UserResponse

    @PUT("user-auth-service/api/auth/me/password")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body request: ChangePasswordRequest
    )

    @DELETE("user-auth-service/api/auth/me")
    suspend fun deleteAccount(@Header("Authorization") authorization: String)
}
