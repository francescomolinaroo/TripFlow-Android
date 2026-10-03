package com.tripflow.feature.booking.ui

import com.tripflow.core.model.UiState

data class BookingScreenUiState(
    val trip: UiState<TripSummaryUi> = UiState.Loading,
    val participants: Int = 1,
    val maxParticipants: Int = 1,
    val notes: String = "",
    val activities: List<ActivityUi> = emptyList(),
    val basePricePerPerson: Int = 0,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdBookingId: String? = null
) {
    val totalPrice: Int
        get() = (basePricePerPerson * participants) + activities
            .filter { it.isSelected }
            .sumOf { it.price * participants }
}
