package com.tripflow.core.network.review.dto

data class ReviewResponseDTO(
    val id: String,
    val tripId: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)