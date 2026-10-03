package com.tripflow.feature.booking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.feature.booking.mapper.toSummaryUi
import com.tripflow.feature.booking.mapper.toUi
import com.tripflow.feature.booking.model.PrenotazioneRequest
import com.tripflow.feature.booking.repository.BookingRepository
import com.tripflow.feature.booking.repository.BookingRepositoryProvider
import com.tripflow.feature.booking.repository.CatalogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class BookingScreenViewModel(
    private val viaggioId: String?,
    private val repository: BookingRepository = BookingRepositoryProvider.repository,
    private val catalogo: CatalogoRepository = BookingRepositoryProvider.catalogo
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingScreenUiState())
    val uiState: StateFlow<BookingScreenUiState> = _uiState.asStateFlow()

    init {
        loadTrip()
    }

    fun loadTrip() {
        if (viaggioId == null) {
            _uiState.update {
                it.copy(trip = UiState.Error("Nessun viaggio selezionato", retryable = false))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(trip = UiState.Loading) }
            when (val result = catalogo.trovaViaggio(viaggioId)) {
                is UiState.Success -> _uiState.update { it.withTrip(result.data) }
                is UiState.Error -> _uiState.update { it.copy(trip = result) }
                else -> Unit
            }
        }
    }

    fun onParticipantsChange(count: Int) {
        _uiState.update { it.copy(participants = count) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onToggleActivity(activityId: String) {
        _uiState.update { state ->
            val updatedActivities = state.activities.map { activity ->
                if (activity.id == activityId) {
                    activity.copy(isSelected = !activity.isSelected)
                } else {
                    activity
                }
            }
            state.copy(activities = updatedActivities)
        }
    }

    fun onConfirm() {
        val state = _uiState.value
        if (viaggioId == null || state.isSubmitting || state.trip !is UiState.Success) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            val request = PrenotazioneRequest(
                viaggioId = viaggioId,
                numeroPartecipanti = state.participants,
                note = state.notes.ifBlank { null },
                attivitaIds = state.activities.filter { it.isSelected }.map { it.id }
            )
            when (val result = repository.creaPrenotazione(request)) {
                is UiState.Success -> _uiState.update {
                    it.copy(isSubmitting = false, createdBookingId = result.data.id)
                }
                is UiState.Error -> _uiState.update {
                    it.copy(isSubmitting = false, submitError = result.message)
                }
                else -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun onNavigatedToPayment() {
        _uiState.update { it.copy(createdBookingId = null) }
    }

    private fun BookingScreenUiState.withTrip(trip: TripResponseDTO): BookingScreenUiState {
        val max = trip.availableSpots.coerceIn(0, MAX_PARTECIPANTI)
        return copy(
            trip = UiState.Success(trip.toSummaryUi()),
            basePricePerPerson = BigDecimal.valueOf(trip.price).setScale(0, RoundingMode.HALF_UP).toInt(),
            activities = trip.activities.orEmpty().map { it.toUi() },
            maxParticipants = max,
            participants = participants.coerceIn(1, max.coerceAtLeast(1))
        )
    }

    private companion object {
        const val MAX_PARTECIPANTI = 50
    }
}
