package com.tripflow.feature.booking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.tripflow.core.ui.component.Status
import com.tripflow.core.ui.component.StatusChip
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.booking.ui.ReceivedBookingUi

@Composable
fun ReceivedBookingCard(
    booking: ReceivedBookingUi,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TripFlowColors.Surface)
            .clickable(onClick = onClick)
            .padding(Dimens.gapL),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                booking.travelerName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            StatusChip(style = Status.prenotazione(booking.status))
        }
        Text(
            "${booking.tripTitle} · ${booking.tripDate}",
            style = MaterialTheme.typography.bodySmall,
            color = TripFlowColors.TextSecondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${booking.participants}  ·  ${booking.totalPrice}",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextBody,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Prenotata il ${booking.bookedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextSecondary
            )
        }
    }
}
