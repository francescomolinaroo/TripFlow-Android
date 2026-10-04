package com.tripflow.feature.review.ui.components

data class ReviewSummaryUi(
    val average: Double,
    val count: Int,
    val distribution: Map<Int, Int>
)
