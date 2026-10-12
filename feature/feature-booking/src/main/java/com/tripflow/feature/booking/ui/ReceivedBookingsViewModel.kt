package com.tripflow.feature.booking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.core.model.toUiState
import com.tripflow.core.ui.format.Formatters
import com.tripflow.feature.booking.mapper.toReceivedUi
import com.tripflow.feature.booking.model.PrenotazioneResponse
import com.tripflow.feature.booking.model.StatoPrenotazione
import com.tripflow.feature.booking.repository.BookingRepository
import com.tripflow.feature.booking.repository.BookingRepositoryProvider
import java.math.BigDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceivedBookingsViewModel(
    private val repository: BookingRepository = BookingRepositoryProvider.repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceivedBookingsUiState())
    val uiState: StateFlow<ReceivedBookingsUiState> = _uiState.asStateFlow()

    private var ricevute: List<PrenotazioneResponse> = emptyList()
    private var loadJob: Job? = null

    fun loadBookings() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(bookings = UiState.Loading) }
            when (val result = repository.prenotazioniRicevute()) {
                is UiState.Success -> {
                    ricevute = result.data.sortedByDescending { it.dataPrenotazione.orEmpty() }
                    aggiornaVista()
                }
                is UiState.Error -> _uiState.update { it.copy(bookings = result, summary = null) }
                else -> Unit
            }
        }
    }

    fun onStatusSelected(stato: StatoPrenotazione?) {
        _uiState.update { it.copy(selectedStatus = stato) }
        aggiornaVista()
    }

    fun onTripSelected(tripId: String?) {
        _uiState.update { it.copy(selectedTripId = tripId) }
        aggiornaVista()
    }

    private fun aggiornaVista() {
        if (ricevute.isEmpty()) {
            _uiState.update {
                it.copy(
                    bookings = UiState.Empty("Quando qualcuno prenoterà uno dei tuoi viaggi la troverai qui"),
                    trips = emptyList(),
                    summary = null
                )
            }
            return
        }

        val state = _uiState.value
        val delViaggio = ricevute.filter { state.selectedTripId == null || it.viaggioId == state.selectedTripId }
        val filtrate = delViaggio.filter { state.selectedStatus == null || it.stato == state.selectedStatus }

        _uiState.update {
            it.copy(
                bookings = filtrate.map { p -> p.toReceivedUi() }.toUiState("Nessuna prenotazione con questi filtri"),
                trips = viaggiDisponibili(),
                summary = riepilogo(delViaggio)
            )
        }
    }

    private fun viaggiDisponibili(): List<TripFilterUi> =
        ricevute
            .distinctBy { it.viaggioId }
            .map { TripFilterUi(id = it.viaggioId, title = it.titoloViaggio ?: "Viaggio") }
            .sortedBy { it.title }

    private fun riepilogo(prenotazioni: List<PrenotazioneResponse>): ReceivedSummaryUi {
        val confermate = prenotazioni.filter {
            it.stato == StatoPrenotazione.CONFERMATA || it.stato == StatoPrenotazione.COMPLETATA
        }
        var incasso = BigDecimal.ZERO
        for (p in confermate) {
            incasso = incasso.add(p.prezzoTotale)
        }
        return ReceivedSummaryUi(
            confirmedBookings = confermate.size,
            confirmedParticipants = confermate.sumOf { it.numeroPartecipanti },
            revenue = Formatters.money(incasso)
        )
    }
}
