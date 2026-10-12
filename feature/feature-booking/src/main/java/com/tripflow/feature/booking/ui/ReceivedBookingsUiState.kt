package com.tripflow.feature.booking.ui

import com.tripflow.core.model.UiState
import com.tripflow.feature.booking.model.StatoPrenotazione

data class ReceivedBookingsUiState(
    val bookings: UiState<List<ReceivedBookingUi>> = UiState.Loading,
    val trips: List<TripFilterUi> = emptyList(),
    val summary: ReceivedSummaryUi? = null,
    val selectedStatus: StatoPrenotazione? = null,
    val selectedTripId: String? = null
)
