package com.tripflow.feature.booking.api

import com.tripflow.feature.booking.model.PagamentoIntentResponse
import com.tripflow.feature.booking.model.PagamentoResponse
import com.tripflow.feature.booking.model.PrenotazioneAttivitaRequest
import com.tripflow.feature.booking.model.PrenotazioneRequest
import com.tripflow.feature.booking.model.PrenotazioneResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BookingApi {
    @POST("booking-service/api/prenotazioni")
    suspend fun crea(@Body request: PrenotazioneRequest): PrenotazioneResponse

    @GET("booking-service/api/prenotazioni/{id}")
    suspend fun trova(@Path("id") id: String): PrenotazioneResponse

    @GET("booking-service/api/prenotazioni/mie")
    suspend fun mie(): List<PrenotazioneResponse>

    @GET("booking-service/api/prenotazioni/mie/attive")
    suspend fun mieAttive(): List<PrenotazioneResponse>

    @PATCH("booking-service/api/prenotazioni/{id}/annulla")
    suspend fun annulla(@Path("id") id: String): PrenotazioneResponse

    @POST("booking-service/api/prenotazioni/{id}/attivita")
    suspend fun aggiungiAttivita(@Path("id") id: String, @Body request: PrenotazioneAttivitaRequest): PrenotazioneResponse

    @DELETE("booking-service/api/prenotazioni/{id}/attivita/{attivitaId}")
    suspend fun rimuoviAttivita(@Path("id") id: String, @Path("attivitaId") attivitaId: String): PrenotazioneResponse

    @POST("booking-service/api/pagamenti/{prenotazioneId}")
    suspend fun avviaPagamento(@Path("prenotazioneId") id: String): PagamentoIntentResponse

    @GET("booking-service/api/pagamenti/{prenotazioneId}")
    suspend fun trovaPagamento(@Path("prenotazioneId") id: String): PagamentoResponse
}