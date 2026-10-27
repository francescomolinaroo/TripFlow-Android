package com.tripflow.core.network.review

import com.tripflow.core.network.review.dto.ReviewResponseDTO
import retrofit2.http.GET
import retrofit2.http.Path

interface ReviewApi {
    @GET("review-service/api/recensioni/oggetto/{tripId}")
    suspend fun getReviewsByTripId(
        @Path("tripId") tripId: String
    ): List<ReviewResponseDTO>

    @GET("review-service/api/recensioni/oggetto/{tripId}/media")
    suspend fun getAverageRating(
        @Path("tripId") tripId: String
    ): Double

    @GET("review-service/api/recensioni/oggetto/{tripId}/conta")
    suspend fun getReviewCount(
        @Path("tripId") tripId: String
    ): Long
}