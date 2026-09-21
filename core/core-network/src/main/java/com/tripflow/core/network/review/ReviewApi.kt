package com.tripflow.core.network.review

import com.tripflow.core.network.review.dto.ReviewResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface ReviewApi {
    @GET("review-service/api/v1/reviews")
    suspend fun getReviewsByTripId(
        @Query("tripId") tripId: String
    ): List<ReviewResponseDTO>
}