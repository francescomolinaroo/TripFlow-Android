package com.tripflow.feature.review.ui

import androidx.lifecycle.ViewModel
import com.tripflow.feature.review.ui.components.ReviewUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReviewListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewListUiState())
    val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

    private val mockReviews = listOf(
        ReviewUi(
            name = "Giulia Rinaldi",
            rating = 5.0,
            date = "2 ago 2026",
            title = "Organizzazione impeccabile",
            comment = "Il sentiero degli Dei vale da solo il viaggio. Hotel ottimo, trasferimenti puntuali, gruppo piccolo."
        ),
        ReviewUi(
            name = "Andrea Moretti",
            rating = 4.0,
            date = "28 lug 2026",
            title = "Bello, ma tanti spostamenti",
            comment = "I trasferimenti in bus sono lunghi e le strade strette. Il tour in barca però lo rifarei domani.",
            isModified = true
        ),
        ReviewUi(
            name = "Luca Conti",
            rating = 5.0,
            date = "19 lug 2026",
            title = "Ci torno l'anno prossimo",
            comment = "Marco conosce ogni angolo della costiera."
        )
    )

    init {
        loadReviews()
    }

    private fun loadReviews() {
        _uiState.update { it.copy(reviews = mockReviews, isLoading = false) }
    }
}
