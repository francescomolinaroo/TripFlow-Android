package com.tripflow.feature.catalog.data.repository

import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.review.dto.ReviewResponseDTO
import com.tripflow.feature.catalog.domain.repository.ReviewRepository

class ReviewRepositoryImpl : ReviewRepository {
    override suspend fun getReviewsByTripId(tripId: String): Result<List<ReviewResponseDTO>> {
        return runCatching {
            ApiClient.reviewApi.getReviewsByTripId(tripId)
        }
    }

    override suspend fun getAverageRating(tripId: String): Result<Double> {
        return runCatching {
            ApiClient.reviewApi.getAverageRating(tripId)
        }
    }

    override suspend fun getReviewCount(tripId: String): Result<Long> {
        return runCatching {
            ApiClient.reviewApi.getReviewCount(tripId)
        }
    }
}