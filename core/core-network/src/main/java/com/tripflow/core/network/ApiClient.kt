package com.tripflow.core.network

import com.tripflow.core.network.auth.AuthApi
import com.tripflow.core.network.catalog.CatalogApi
import com.tripflow.core.network.review.ReviewApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Fornisce il token JWT salvato (impostato da TokenStorage / Auth)
    var tokenProvider: (() -> String?)? = null

    // Da' all'Authenticator l'accesso ai token salvati per rinnovarli sul 401 (impostato da TokenStorage).
    var sessionTokenStore: SessionTokenStore? = null

    // Client "nudo" per la sola chiamata di refresh: niente interceptor che allega il token scaduto
    // (altrimenti Spring risponde 401 anche su /auth/refresh) e niente authenticator, cosi' un 401
    // sul refresh non rientra in un ciclo infinito.
    private val refreshRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = tokenProvider?.invoke()
                if (!token.isNullOrBlank() && chain.request().header("Authorization") == null) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            // Rinnova il token e riprova la richiesta quando il backend risponde 401 (token scaduto).
            .authenticator(
                TokenAuthenticator(
                    storeProvider = { sessionTokenStore },
                    refreshApi = refreshRetrofit.create(AuthApi::class.java),
                )
            )
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    val catalogApi: CatalogApi by lazy { retrofit.create(CatalogApi::class.java) }

    val reviewApi: ReviewApi by lazy { retrofit.create(ReviewApi::class.java) }
}