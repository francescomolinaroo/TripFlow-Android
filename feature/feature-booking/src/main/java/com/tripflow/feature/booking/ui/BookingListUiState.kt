package com.tripflow.feature.booking.ui

data class BookingListUiState(
    val bookings: List<BookingUi> = emptyList(),
    val selectedTabIndex: Int = 0,
    val isLoading: Boolean = false
)
