package com.tripflow.feature.booking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tripflow.core.ui.component.Status
import com.tripflow.core.ui.component.StatusChip
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.booking.ui.BookingUi

@Composable
fun BookingCard(
    booking: BookingUi,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TripFlowColors.Surface)
            .clickable(onClick = onClick)
            .padding(Dimens.gapM),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = null,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(Dimens.radiusChip))
                    .background(TripFlowColors.Surface2),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(booking.title, style = MaterialTheme.typography.titleMedium)
                    StatusChip(style = Status.prenotazione(booking.status))
                }
                Text("${booking.date} · ${booking.location}", style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
                Text(
                    "${booking.participants} partecipant${if (booking.participants > 1) "i" else "e"}  ·  € ${booking.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextBody,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (booking.action != null) {
            HorizontalDivider(color = TripFlowColors.Divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onActionClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (booking.actionIcon != null) {
                        Icon(
                            booking.actionIcon,
                            contentDescription = null,
                            tint = if (booking.status == "IN_ATTESA") TripFlowColors.Warning else TripFlowColors.Accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        booking.action,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (booking.status == "IN_ATTESA") TripFlowColors.Warning else TripFlowColors.Accent
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TripFlowColors.TextSecondary)
            }
        }
    }
}
