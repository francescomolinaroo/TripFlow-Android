package com.tripflow.feature.itinerary.ui.myitineraries

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.itinerary.model.MyItinerary

@Composable
fun MyItineraryCard(
    itinerary: MyItinerary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val previewStops = itinerary.stopsPreview.take(2)
    val remainingStops = itinerary.stopsCount - previewStops.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TripFlowColors.Background, shape)
            .border(1.dp, TripFlowColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(Dimens.gapM),
    ) {
        // Header: Title + StatusChip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = itinerary.title,
                style = MaterialTheme.typography.titleMedium,
                color = TripFlowColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            VisibilityStatusChip(isPublic = itinerary.isPublic)
        }

        // Subtitle: Date range + stops count
        Text(
            text = "${itinerary.dateRange} · ${itinerary.stopsCount} tappe",
            style = MaterialTheme.typography.bodySmall,
            color = TripFlowColors.TextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.gapS),
        )

        // Preview stops
        if (previewStops.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.gapM),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapS),
            ) {
                previewStops.forEachIndexed { index, stop ->
                    StopPreviewRow(
                        stop = stop,
                        isFirst = index == 0,
                        colors = listOf(
                            Color(0xFF8B5CF6), // Violet
                            Color(0xFF3B82F6)  // Blue
                        )
                    )
                }
            }
        }

        // Remaining stops indicator
        if (remainingStops > 0) {
            Text(
                text = "• e altre $remainingStops tappe",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.gapS),
            )
        }
    }
}

@Composable
private fun StopPreviewRow(
    stop: com.tripflow.feature.itinerary.model.StopPreview,
    isFirst: Boolean,
    colors: List<Color>
) {
    val dotColor = colors[if (isFirst) 0 else 1 % colors.size]

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapS),
    ) {
        // Colored dot with connecting line
        Box(
            modifier = Modifier
                .width(20.dp)
                .padding(vertical = 2.dp)
        ) {
            // Vertical line connecting dots
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(TripFlowColors.Divider)
                        .align(Alignment.CenterStart)
                )
            }
            // Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(dotColor, RoundedCornerShape(5.dp))
                    .align(Alignment.CenterStart)
            )
        }

        // Stop info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = stop.title,
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stop.time,
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextSecondary,
            )
        }
    }
}