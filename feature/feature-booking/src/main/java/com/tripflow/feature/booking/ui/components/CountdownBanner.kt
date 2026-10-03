package com.tripflow.feature.booking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun CountdownBanner(secondsLeft: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(TripFlowColors.WarningSoft)
            .padding(Dimens.gapL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            tint = TripFlowColors.Warning,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                "Posti bloccati ancora per ${formatCountdown(secondsLeft)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TripFlowColors.Warning
            )
            Text(
                "Completa il pagamento prima della scadenza",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextBody
            )
        }
    }
}

private fun formatCountdown(seconds: Long): String =
    "%d:%02d".format(seconds / 60, seconds % 60)
