package com.tripflow.feature.review.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.core.model.toUiState
import com.tripflow.feature.review.mapper.toSummaryUi
import com.tripflow.feature.review.mapper.toUi
import com.tripflow.feature.review.repository.ReviewRepository
import com.tripflow.feature.review.repository.ReviewRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewListViewModel(
    private val oggettoId: String?,
    private val repository: ReviewRepository = ReviewRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewListUiState(isMine = oggettoId == null))
    val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadReviews() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(reviews = UiState.Loading) }

            val result = if (oggettoId == null) {
                repository.mieRecensioni()
            } else {
                repository.recensioniOggetto(oggettoId)
            }

            when (result) {
                is UiState.Success -> {
                    val recensioni = result.data
                    val messaggioVuoto = if (oggettoId == null) {
                        "Le recensioni che scrivi compariranno qui"
                    } else {
                        "Nessuno ha ancora recensito questo viaggio"
                    }
                    _uiState.update {
                        it.copy(
                            reviews = recensioni.toUi(conOggetto = oggettoId == null).toUiState(messaggioVuoto),
                            summary = recensioni.toSummaryUi(),
                            subjectName = recensioni.firstOrNull()?.oggettoNome
                        )
                    }
                }
                is UiState.Error -> _uiState.update { it.copy(reviews = result) }
                is UiState.Empty -> _uiState.update { it.copy(reviews = result) }
                UiState.Loading -> Unit
            }
        }
    }
}
