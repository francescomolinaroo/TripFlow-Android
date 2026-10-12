package com.tripflow.feature.booking.ui

data class BookingDetailUi(
    val id: String,
    val trip: TripSummaryUi,
    val status: String,
    val participants: Int,
    val unitPrice: String,
    val totalPrice: String,
    val activities: List<ActivityUi>,
    val notes: String?,
    val paymentStatus: String?,
    val paymentDetail: String?,
    val canPay: Boolean,
    val canCancel: Boolean
)
