package com.tripflow.feature.review.ui

import com.tripflow.feature.review.ui.components.ReviewUi

data class ReviewListUiState(
    val reviews: List<ReviewUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
