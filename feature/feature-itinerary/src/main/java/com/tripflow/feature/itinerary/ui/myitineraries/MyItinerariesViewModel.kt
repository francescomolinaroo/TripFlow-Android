package com.tripflow.feature.itinerary.ui.myitineraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.MyItinerary
import com.tripflow.feature.itinerary.model.StopPreview
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import com.tripflow.feature.itinerary.session.CurrentUserProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

// Il repository reale (ItineraryRepositoryImpl) arriva da Hilt tramite ItineraryModule.
// FakeItineraryRepository resta solo per Preview e test, che lo passano esplicitamente.
@HiltViewModel
class MyItinerariesViewModel @Inject constructor(
    private val repository: ItineraryRepository,
    private val currentUserProvider: CurrentUserProvider = CurrentUserProvider.None,
) : ViewModel() {

    // Codice utente (id Keycloak) da comunicare a chi vuole condividere un itinerario con te.
    val myUserCode: String? by lazy { currentUserProvider.currentUserId() }

    private val _uiState = MutableStateFlow<UiState<List<MyItinerary>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var hasBeenResumed = false

    init {
        loadItineraries()
    }

    fun loadItineraries() {
        // Un nuovo caricamento annulla quello in corso: vince sempre l'ultimo.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = repository.getMyItineraries()) {
                // Lista vuota -> stato Empty: mostra "Nessun itinerario" con il pulsante di creazione
                // invece di una schermata bianca.
                is UiState.Success ->
                    if (result.data.isEmpty()) UiState.Empty(null)
                    else UiState.Success(result.data.map { it.toMyItinerary() })
                is UiState.Empty -> UiState.Empty(result.message)
                is UiState.Error -> result
                UiState.Loading -> UiState.Loading
            }
        }
    }

    // Chiamato a ogni ON_RESUME della schermata. Il primo resume coincide con il
    // caricamento di init e viene ignorato; i successivi (ritorno da dettaglio o
    // creazione) ricaricano la lista, cosi' un itinerario appena creato compare subito.
    fun onScreenResumed() {
        if (hasBeenResumed) {
            loadItineraries()
        } else {
            hasBeenResumed = true
        }
    }

    fun onItineraryDeleted() {
        loadItineraries()
    }

    fun onItineraryUpdated() {
        loadItineraries()
    }
}

private fun ItinerarySummary.toMyItinerary(): MyItinerary = MyItinerary(
    id = id,
    title = title,
    dateRange = formatDateRange(startDate, endDate),
    stopsCount = stagesCount,
    visibility = visibility,
    stopsPreview = previewStages.map { StopPreview(title = it.title, time = it.startTime) },
)

private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)
private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

private fun formatDateRange(start: LocalDate, end: LocalDate): String = when {
    start.year == end.year && start.month == end.month ->
        "${start.dayOfMonth} - ${end.format(dayMonthYear)}"
    start.year == end.year ->
        "${start.format(dayMonth)} - ${end.format(dayMonthYear)}"
    else ->
        "${start.format(dayMonthYear)} - ${end.format(dayMonthYear)}"
}
