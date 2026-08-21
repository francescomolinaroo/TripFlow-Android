package com.tripflow.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

//per ora questi campi, si può aggiungere, togliere, modificare o creare una coard specifica a parte se serve
data class TripCardUi(
    val id: String,
    val title: String,
    val destination: String,
    val dateRange: String,
    val priceLabel: String,
    val imageUrl: String?,
    val rating: Double?,
    val spotsLeft: Int?,
)

@Composable
fun TripCard(
    trip: TripCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, TripFlowColors.Border, shape)
            .clickable(onClick = onClick),
    ) {

        Box {
            AsyncImage(
                model = trip.imageUrl,
                contentDescription = trip.title,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(TripFlowColors.Surface2),
                error = ColorPainter(TripFlowColors.Surface2),
                fallback = ColorPainter(TripFlowColors.Surface2),
                modifier = Modifier.fillMaxWidth().height(148.dp),
            )
            if (trip.rating != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(TripFlowColors.Background)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    RatingStars(
                        rating = trip.rating,
                        starSize = 12.dp,
                        showValue = true,
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TripFlowColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = trip.priceLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = TripFlowColors.TextPrimary,
                    )
                    Text(
                        text = "a persona",
                        style = MaterialTheme.typography.bodySmall,
                        color = TripFlowColors.TextSecondary,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = trip.dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary,
                    maxLines = 1,
                )
                if (trip.spotsLeft != null) {
                    SpotsBadge(trip.spotsLeft)
                }
            }
        }
    }
}

@Composable
private fun SpotsBadge(spotsLeft: Int) {
    val style = when {
        spotsLeft <= 0 -> StatusStyle("Esaurito", TripFlowColors.TextSecondary, TripFlowColors.Surface3)
        spotsLeft < 5 -> StatusStyle("$spotsLeft posti", TripFlowColors.Warning, TripFlowColors.WarningSoft)
        else -> StatusStyle("$spotsLeft posti", TripFlowColors.AccentDark, TripFlowColors.AccentSoft)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.radiusChip))
            .background(style.container)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(text = style.label, style = MaterialTheme.typography.bodySmall, color = style.content)
    }
}
