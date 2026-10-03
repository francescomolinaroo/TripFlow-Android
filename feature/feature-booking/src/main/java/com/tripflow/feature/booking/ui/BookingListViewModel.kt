package com.tripflow.feature.booking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.core.model.toUiState
import com.tripflow.feature.booking.mapper.toUi
import com.tripflow.feature.booking.model.PrenotazioneResponse
import com.tripflow.feature.booking.repository.BookingRepository
import com.tripflow.feature.booking.repository.BookingRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingListViewModel(
    private val repository: BookingRepository = BookingRepositoryProvider.repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingListUiState())
    val uiState: StateFlow<BookingListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadBookings() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(bookings = UiState.Loading) }
            val result = if (_uiState.value.selectedTabIndex == 0) {
                repository.miePrenotazioniAttive()
            } else {
                repository.miePrenotazioni()
            }
            _uiState.update { it.copy(bookings = result.toBookingsUi()) }
        }
    }

    fun onTabSelected(index: Int) {
        if (index == _uiState.value.selectedTabIndex) return
        _uiState.update { it.copy(selectedTabIndex = index) }
        loadBookings()
    }

    private fun UiState<List<PrenotazioneResponse>>.toBookingsUi(): UiState<List<BookingUi>> =
        when (this) {
            is UiState.Success -> data.toUi().toUiState("Quando prenoterai un viaggio lo troverai qui")
            is UiState.Error -> this
            is UiState.Empty -> this
            UiState.Loading -> UiState.Loading
        }
}
