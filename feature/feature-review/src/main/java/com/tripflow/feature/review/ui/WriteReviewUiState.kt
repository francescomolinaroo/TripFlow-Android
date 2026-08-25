package com.tripflow.feature.review.ui

data class WriteReviewUiState(
    val rating: Int = 4,
    val title: String = "",
    val comment: String = "",
    val isSubmitted: Boolean = false,
    val isLoading: Boolean = false,
    val isPublished: Boolean = false,
    val error: String? = null
) {
    val titleError: String? = if (isSubmitted && title.isBlank()) "Inserisci un titolo per la recensione" else null
    val commentError: String? = if (isSubmitted && comment.isBlank()) "Racconta la tua esperienza" else null
    val isValid: Boolean = title.isNotBlank() && comment.isNotBlank()
    
    val feedbackText: String = when(rating) {
        1 -> "Pessimo"
        2 -> "Deludente"
        3 -> "Nella media"
        4 -> "Molto bello"
        5 -> "Eccellente"
        else -> ""
    }
}
