package com.tripflow.feature.itinerary.ui.myitineraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.MyItinerary
import com.tripflow.feature.itinerary.model.StopPreview
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MyItinerariesViewModel(
    private val repository: ItineraryRepository = FakeItineraryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<MyItinerary>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadItineraries()
    }

    fun loadItineraries() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = repository.getMyItineraries()) {
                is UiState.Success -> UiState.Success(result.data.map { it.toMyItinerary() })
                is UiState.Empty -> UiState.Empty(result.message)
                is UiState.Error -> result
                UiState.Loading -> UiState.Loading
            }
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
