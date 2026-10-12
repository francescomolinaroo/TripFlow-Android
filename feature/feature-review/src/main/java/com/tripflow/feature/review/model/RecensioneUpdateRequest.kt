package com.tripflow.feature.review.model

data class RecensioneUpdateRequest(
    val valutazione: Int,
    val titolo: String?,
    val commento: String?
)