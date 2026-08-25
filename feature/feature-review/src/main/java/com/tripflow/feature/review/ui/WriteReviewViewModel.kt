package com.tripflow.feature.review.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WriteReviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WriteReviewUiState())
    val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

    fun onRatingChange(rating: Int) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onCommentChange(comment: String) {
        _uiState.update { it.copy(comment = comment) }
    }

    fun publishReview(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isSubmitted = true) }
        
        if (_uiState.value.isValid) {
            _uiState.update { it.copy(isLoading = true) }

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isPublished = true
                )
            }
            onSuccess()
        }
    }
}
