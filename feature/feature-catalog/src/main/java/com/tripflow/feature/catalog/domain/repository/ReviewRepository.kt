package com.tripflow.feature.catalog.domain.repository

import com.tripflow.core.network.review.dto.ReviewResponseDTO

interface ReviewRepository {
    suspend fun getReviewsByTripId(tripId: String): Result<List<ReviewResponseDTO>>
}