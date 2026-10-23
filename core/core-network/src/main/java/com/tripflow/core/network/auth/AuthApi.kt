package com.tripflow.core.network.auth

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface AuthApi {
    @POST("user-auth-service/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("user-auth-service/api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("user-auth-service/api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): TokenRefreshResponse

    @POST("user-auth-service/api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    @POST("user-auth-service/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest)

    @POST("user-auth-service/api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest)

    @POST("user-auth-service/api/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest)

    @POST("user-auth-service/api/auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest)

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

    @GET("user-auth-service/api/auth/users/by-keycloak/{keycloakId}")
    suspend fun getPublicUserByKeycloakId(@Path("keycloakId") keycloakId: String): PublicUserResponse

    @GET("user-auth-service/api/auth/users/{id}")
    suspend fun getPublicUserById(@Path("id") id: String): PublicUserResponse

    @POST("user-auth-service/api/auth/users/lookup")
    suspend fun lookupUsers(
        @Header("Authorization") authorization: String,
        @Body request: UserLookupRequest
    ): List<PublicUserResponse>

    @POST("user-auth-service/api/auth/users/lookup-by-ids")
    suspend fun lookupUsersByKeycloakIds(
        @Header("Authorization") authorization: String,
        @Body request: UserLookupByKeycloakIdsRequest
    ): List<PublicUserResponse>

    @Multipart
    @POST("user-auth-service/api/auth/me/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part
    ): Map<String, String>
}
