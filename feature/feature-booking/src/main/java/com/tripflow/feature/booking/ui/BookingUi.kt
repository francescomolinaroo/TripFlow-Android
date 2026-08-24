package com.tripflow.feature.booking.ui

import androidx.compose.ui.graphics.vector.ImageVector

data class BookingUi(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val participants: Int,
    val price: Int,
    val status: String,
    val action: String? = null,
    val actionIcon: ImageVector? = null
)
