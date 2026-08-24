package com.tripflow.core.network

import com.tripflow.core.network.auth.AuthApi
import com.tripflow.core.network.auth.KeycloakApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    val keycloakApi: KeycloakApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9090/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KeycloakApi::class.java)
    }
}
