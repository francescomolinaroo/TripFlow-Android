package com.tripflow.feature.review.api

import com.tripflow.feature.review.model.RecensioneRequest
import com.tripflow.feature.review.model.RecensioneResponse
import com.tripflow.feature.review.model.RecensioneUpdateRequest
import com.tripflow.feature.review.model.TipoOggetto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApi {

    @POST("review-service/api/recensioni")
    suspend fun crea(@Body request: RecensioneRequest): RecensioneResponse

    @GET("review-service/api/recensioni/mie")
    suspend fun mie(): List<RecensioneResponse>

    @PUT("review-service/api/recensioni/{id}")
    suspend fun modifica(@Path("id") id: String, @Body request: RecensioneUpdateRequest): RecensioneResponse

    @DELETE("review-service/api/recensioni/{id}")
    suspend fun elimina(@Path("id") id: String)

    @GET("review-service/api/recensioni/puo-recensire")
    suspend fun puoRecensire(
        @Query("prenotazioneId") prenotazioneId: String,
        @Query("tipoOggetto") tipoOggetto: TipoOggetto,
        @Query("oggettoId") oggettoId: String
    ): Boolean

    @GET("review-service/api/recensioni/{id}")
    suspend fun trova(@Path("id") id: String): RecensioneResponse

    @GET("review-service/api/recensioni/oggetto/{oggettoId}")
    suspend fun perOggetto(@Path("oggettoId") oggettoId: String): List<RecensioneResponse>

    @GET("review-service/api/recensioni/oggetto/{oggettoId}/media")
    suspend fun media(@Path("oggettoId") oggettoId: String): Double

    @GET("review-service/api/recensioni/oggetto/{oggettoId}/conta")
    suspend fun conta(@Path("oggettoId") oggettoId: String): Long
}
