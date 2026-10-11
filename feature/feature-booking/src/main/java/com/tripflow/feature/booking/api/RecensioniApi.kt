package com.tripflow.feature.booking.api

import com.tripflow.feature.booking.model.RecensioneInviataResponse
import retrofit2.http.GET

interface RecensioniApi {
    @GET("review-service/api/recensioni/mie")
    suspend fun mie(): List<RecensioneInviataResponse>
}
