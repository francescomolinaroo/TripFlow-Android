package com.tripflow.feature.booking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.booking.ui.TripSummaryUi

@Composable
fun TripSummaryHeader(trip: TripSummaryUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(TripFlowColors.Surface)
            .padding(Dimens.gapM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        AsyncImage( //immagine a lato, da rivedere. Nota: da considerare sfondo del viaggio nell'header
            model = trip.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(Dimens.radiusChip))
                .background(TripFlowColors.Surface2),
            contentScale = ContentScale.Crop
        )
        Column {
            Text(trip.title, style = MaterialTheme.typography.titleMedium)
            Text("${trip.date} · ${trip.location}", style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
        }
    }
}
