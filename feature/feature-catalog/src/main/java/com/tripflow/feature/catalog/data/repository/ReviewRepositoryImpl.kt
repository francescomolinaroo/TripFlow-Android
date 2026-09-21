package com.tripflow.feature.catalog.data.repository

import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.review.dto.ReviewResponseDTO
import com.tripflow.feature.catalog.domain.repository.ReviewRepository

class ReviewRepositoryImpl : ReviewRepository {
    override suspend fun getReviewsByTripId(tripId: String): Result<List<ReviewResponseDTO>> {
        return try {
            val response = ApiClient.reviewApi.getReviewsByTripId(tripId)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}