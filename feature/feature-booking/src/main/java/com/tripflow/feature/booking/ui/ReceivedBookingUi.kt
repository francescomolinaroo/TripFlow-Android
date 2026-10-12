package com.tripflow.feature.booking.ui

data class ReceivedBookingUi(
    val id: String,
    val tripId: String,
    val travelerName: String,
    val tripTitle: String,
    val tripDate: String,
    val participants: String,
    val totalPrice: String,
    val status: String,
    val bookedAt: String
)

data class TripFilterUi(
    val id: String,
    val title: String
)

data class ReceivedSummaryUi(
    val confirmedBookings: Int,
    val confirmedParticipants: Int,
    val revenue: String
)
