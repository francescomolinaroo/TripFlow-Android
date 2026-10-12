package com.tripflow.feature.booking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.booking.mapper.toDetailUi
import com.tripflow.feature.booking.model.PrenotazioneResponse
import com.tripflow.feature.booking.model.StatoPagamento
import com.tripflow.feature.booking.model.StatoPrenotazione
import com.tripflow.feature.booking.repository.BookingRepository
import com.tripflow.feature.booking.repository.BookingRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingDetailViewModel(
    private val prenotazioneId: String?,
    private val repository: BookingRepository = BookingRepositoryProvider.repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingDetailUiState())
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var countdownJob: Job? = null
    private var paymentJob: Job? = null

    fun loadBooking() {
        if (prenotazioneId == null) {
            _uiState.update {
                it.copy(booking = UiState.Error("Nessuna prenotazione selezionata", retryable = false))
            }
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (_uiState.value.booking !is UiState.Success) {
                _uiState.update { it.copy(booking = UiState.Loading) }
            }
            when (val result = repository.trovaPrenotazione(prenotazioneId)) {
                is UiState.Success -> apply(result.data)
                is UiState.Error -> _uiState.update { it.copy(booking = result) }
                else -> Unit
            }
        }
    }

    fun onPayClick() {
        if (prenotazioneId == null || _uiState.value.isPaying) return
        paymentJob?.cancel()
        paymentJob = viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true, actionError = null) }
            when (val result = repository.avviaPagamento(prenotazioneId)) {
                is UiState.Success -> waitForConfirmation(prenotazioneId)
                is UiState.Error -> _uiState.update { it.copy(actionError = result.message) }
                else -> Unit
            }
            _uiState.update { it.copy(isPaying = false) }
        }
    }

    fun onCancelClick() {
        _uiState.update { it.copy(showCancelDialog = true, actionError = null) }
    }

    fun onCancelDismiss() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    fun onCancelConfirm() {
        if (prenotazioneId == null || _uiState.value.isCancelling) return
        viewModelScope.launch {
            _uiState.update { it.copy(showCancelDialog = false, isCancelling = true, actionError = null) }
            when (val result = repository.annullaPrenotazione(prenotazioneId)) {
                is UiState.Success -> {
                    paymentJob?.cancel()
                    apply(result.data)
                    _uiState.update { it.copy(isPaying = false) }
                }
                is UiState.Error -> _uiState.update { it.copy(actionError = result.message) }
                else -> Unit
            }
            _uiState.update { it.copy(isCancelling = false) }
        }
    }

    private suspend fun waitForConfirmation(id: String) {
        repeat(MAX_CONTROLLI_PAGAMENTO) {
            delay(INTERVALLO_CONTROLLO_MS)
            val result = repository.trovaPrenotazione(id)
            if (result is UiState.Success) {
                apply(result.data)
                val pagamento = result.data.infoPagamento?.stato
                if (result.data.stato != StatoPrenotazione.IN_ATTESA) return
                if (pagamento == StatoPagamento.FALLITO) {
                    _uiState.update { it.copy(actionError = "Pagamento non riuscito, riprova") }
                    return
                }
            }
        }
        _uiState.update {
            it.copy(actionError = "La conferma del pagamento non è ancora arrivata. Riprova più tardi.")
        }
    }

    private fun apply(prenotazione: PrenotazioneResponse) {
        val secondi = prenotazione.secondiAllaScadenza
            ?.takeIf { prenotazione.stato == StatoPrenotazione.IN_ATTESA && it > 0 }
        _uiState.update {
            it.copy(booking = UiState.Success(prenotazione.toDetailUi()), secondsLeft = secondi)
        }
        startCountdown(secondi)
    }

    private fun startCountdown(secondi: Long?) {
        countdownJob?.cancel()
        if (secondi == null) return
        countdownJob = viewModelScope.launch {
            var rimasti = secondi
            while (rimasti > 0) {
                delay(1000)
                rimasti -= 1
                _uiState.update { it.copy(secondsLeft = rimasti) }
            }
            loadBooking()
        }
    }

    private companion object {
        const val MAX_CONTROLLI_PAGAMENTO = 15
        const val INTERVALLO_CONTROLLO_MS = 2000L
    }
}
