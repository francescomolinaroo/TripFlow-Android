package com.tripflow.feature.itinerary.ui.itinerarylist

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripflow.core.ui.component.Status
import com.tripflow.core.ui.component.StatusChip
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.itinerary.model.ItinerarySummary
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun ItineraryCard(
    itinerary: ItinerarySummary,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateRange = buildDateRange(itinerary.startDate, itinerary.endDate)
    val locale = Locale.getDefault()
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(Dimens.radiusCard))
        .background(TripFlowColors.Surface)
        .border(1.dp, TripFlowColors.Border, RoundedCornerShape(Dimens.radiusCard))
        .clickable(onClick = onClick)
        .padding(Dimens.gapM)
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(Dimens.radiusCard),
        colors = CardDefaults.cardColors(
            containerColor = TripFlowColors.Surface
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
            // Header: titolo + visibility chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itinerary.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = TripFlowColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = Dimens.gapM)
                )
                VisibilityChip(isPublic = itinerary.isPublic)
            }

            // Date + location row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.gapL),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconWithText(
                    icon = Icons.Default.CalendarMonth,
                    text = dateRange,
                    color = TripFlowColors.TextSecondary
                )
                IconWithText(
                    icon = Icons.Default.LocationOn,
                    text = "${itinerary.stagesCount} tappe",
                    color = TripFlowColors.TextSecondary
                )
            }

            // Preview stages (prime 2)
            if (itinerary.previewStages.isNotEmpty()) {
                HorizontalDivider(color = TripFlowColors.Divider)
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                    itinerary.previewStages.take(2).forEach { preview ->
                        PreviewStageRow(preview = preview)
                    }
                    if (itinerary.previewStages.size > 2) {
                        Text(
                            text = "+${itinerary.previewStages.size - 2} altre tappe...",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = TripFlowColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisibilityChip(isPublic: Boolean) {
    val (icon, label, color) = if (isPublic) {
        Icons.Default.Public to "Pubblico" to TripFlowColors.Accent
    } else {
        Icons.Default.Lock to "Privato" to TripFlowColors.TextSecondary
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color.copy(alpha = 0.12f),
                RoundedCornerShape(Dimens.radiusChip)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(
                text = label,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IconWithText(icon: androidx.compose.material.icons.filled.Icon, text: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun PreviewStageRow(preview: ItinerarySummary.StagePreview) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time range
        Column(modifier = Modifier.width(70.dp)) {
            Text(
                text = "${preview.startTime} - ${preview.endTime}",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                color = TripFlowColors.TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        }
        // Title + catalog badge
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = preview.title,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (preview.isFromCatalog) {
                Text(
                    text = "DAL CATALOGO",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = TripFlowColors.Accent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        // Day indicator
        Text(
            text = "Giorno ${preview.dayNumber}",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = TripFlowColors.TextSecondary
        )
    }
}

private fun buildDateRange(startDate: java.time.LocalDate, endDate: java.time.LocalDate): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault())
    return "${startDate.format(formatter)} – ${endDate.format(formatter)}"
}

@Composable
fun ItineraryListScreenPreview() {
    TripFlowTheme {
        val fakeRepo = com.tripflow.feature.itinerary.repository.FakeItineraryRepository()
        val itineraries = fakeRepo.getMyItineraries().getOrNull() ?: emptyList()
        
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.screenPadding),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Dimens.gapM)
        ) {
            androidx.compose.foundation.lazy.items(itineraries) { itinerary ->
                ItineraryCard(itinerary = itinerary, onClick = {})
            }
        }
    }
}