package com.tripflow.feature.review.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.review.mapper.toRequest
import com.tripflow.feature.review.model.TipoOggetto
import com.tripflow.feature.review.repository.ReviewRepository
import com.tripflow.feature.review.repository.ReviewRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WriteReviewViewModel(
    private val prenotazioneId: String?,
    private val oggettoId: String?,
    private val tipoOggetto: TipoOggetto = TipoOggetto.VIAGGIO,
    private val repository: ReviewRepository = ReviewRepositoryImpl()
) : ViewModel() {

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

    fun publishReview() {
        _uiState.update { it.copy(isSubmitted = true, error = null) }

        val state = _uiState.value
        if (!state.isValid || state.isLoading) return

        if (prenotazioneId == null || oggettoId == null) {
            _uiState.update { it.copy(error = "Nessuna prenotazione selezionata") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val request = state.toRequest(
                prenotazioneId = prenotazioneId,
                oggettoId = oggettoId,
                tipoOggetto = tipoOggetto
            )

            when (val result = repository.creaRecensione(request)) {
                is UiState.Success -> _uiState.update { it.copy(isLoading = false, isPublished = true) }
                is UiState.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPublishedHandled() {
        _uiState.update { it.copy(isPublished = false) }
    }
}
