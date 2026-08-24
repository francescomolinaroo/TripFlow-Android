package com.tripflow.feature.booking.ui

data class BookingScreenUiState(
    val participants: Int = 2,
    val notes: String = "",
    val activities: List<ActivityUi> = emptyList(),
    val basePricePerPerson: Int = 480
) {
    val totalPrice: Int
        get() = (basePricePerPerson * participants) + activities
            .filter { it.isSelected }
            .sumOf { it.price * participants }
}
