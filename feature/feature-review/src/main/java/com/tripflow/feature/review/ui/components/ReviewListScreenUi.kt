package com.tripflow.feature.review.ui.components

data class ReviewUi(
    val name: String,
    val rating: Double,
    val date: String,
    val title: String,
    val comment: String,
    val isModified: Boolean = false
)