package com.tripflow.core.network.auth

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface KeycloakApi {
    @FormUrlEncoded
    @POST("realms/tripflow/protocol/openid-connect/token")
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("realms/tripflow/protocol/openid-connect/token")
    suspend fun refresh(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): TokenResponse
}