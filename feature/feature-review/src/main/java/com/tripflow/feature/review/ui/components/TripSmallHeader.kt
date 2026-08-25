package com.tripflow.feature.review.ui.components

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

@Composable
fun TripSmallHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(TripFlowColors.Surface)
            .padding(Dimens.gapM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
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
        Column {
            Text("Isole Eolie in barca", style = MaterialTheme.typography.titleMedium)
            Text("Viaggio completato il 7 giu 2026", style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
        }
    }
}