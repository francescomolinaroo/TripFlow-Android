package com.tripflow.feature.booking.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BookingListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BookingListUiState())
    val uiState: StateFlow<BookingListUiState> = _uiState.asStateFlow()

    private val allBookings = listOf(
        BookingUi(
            id = "1",
            title = "Costa Amalfitana",
            date = "14 – 18 set 2026",
            location = "Amalfi",
            participants = 2,
            price = 1050,
            status = "IN_ATTESA",
            action = "Completa il pagamento",
            actionIcon = Icons.Default.CreditCard
        ),
        BookingUi(
            id = "2",
            title = "Trekking in Val di Funes",
            date = "22 – 27 set 2026",
            location = "Bolzano",
            participants = 1,
            price = 620,
            status = "CONFERMATA",
            action = null
        ),
        BookingUi(
            id = "3",
            title = "Isole Eolie in barca",
            date = "2 – 7 giu 2026",
            location = "Lipari",
            participants = 2,
            price = 890,
            status = "COMPLETATA",
            action = "Scrivi una recensione",
            actionIcon = Icons.Default.Star
        )
    )

    init {
        loadBookings()
    }

    private fun loadBookings() {
        _uiState.update { it.copy(bookings = allBookings, isLoading = false) }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }

}
}
