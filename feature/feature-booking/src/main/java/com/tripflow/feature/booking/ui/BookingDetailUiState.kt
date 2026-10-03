package com.tripflow.feature.booking.ui

import com.tripflow.core.model.UiState

data class BookingDetailUiState(
    val booking: UiState<BookingDetailUi> = UiState.Loading,
    val secondsLeft: Long? = null,
    val isPaying: Boolean = false,
    val isCancelling: Boolean = false,
    val showCancelDialog: Boolean = false,
    val actionError: String? = null
)
