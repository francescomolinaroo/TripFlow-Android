package com.tripflow.feature.review.ui

import com.tripflow.core.model.UiState
import com.tripflow.feature.review.ui.components.ReviewSummaryUi
import com.tripflow.feature.review.ui.components.ReviewUi

data class ReviewListUiState(
    val reviews: UiState<List<ReviewUi>> = UiState.Loading,
    val summary: ReviewSummaryUi? = null,
    val subjectName: String? = null,
    val isMine: Boolean = true
)
