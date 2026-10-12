package com.tripflow.feature.booking.ui

import com.tripflow.core.model.UiState

data class BookingListUiState(
    val bookings: UiState<List<BookingUi>> = UiState.Loading,
    val selectedTabIndex: Int = 0
)
