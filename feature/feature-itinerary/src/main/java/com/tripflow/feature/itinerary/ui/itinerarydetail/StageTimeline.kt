package com.tripflow.feature.itinerary.ui.itinerarydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.StopType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Tappe di un giorno, gia' ordinate per orario.
data class ItineraryDay(
    val dayNumber: Int,
    val date: LocalDate,
    val stages: List<StageDetail>,
)

// Raggruppa le tappe per data. Il numero del giorno parte dall'inizio dell'itinerario
// (una tappa il primo giorno e' "Giorno 1"); dentro il giorno l'ordine e' per orario.
fun groupStagesByDay(stages: List<StageDetail>, itineraryStart: LocalDate): List<ItineraryDay> =
    stages
        .groupBy { it.date }
        .toSortedMap()
        .map { (date, dayStages) ->
            ItineraryDay(
                dayNumber = (ChronoUnit.DAYS.between(itineraryStart, date) + 1).toInt().coerceAtLeast(1),
                date = date,
                stages = dayStages.sortedWith(compareBy({ it.startTime.ifBlank { "99:99" } }, { it.orderIndex })),
            )
        }

private val dayLabelFormatter = DateTimeFormatter.ofPattern("EEE d MMMM", Locale.ITALIAN)

@Composable
internal fun DayHeader(day: ItineraryDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.gapS, bottom = Dimens.gapS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapS),
    ) {
        Surface(color = TripFlowColors.Accent, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = "Giorno ${day.dayNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TripFlowColors.ButtonContent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Text(
            text = day.date.format(dayLabelFormatter),
            style = MaterialTheme.typography.labelLarge,
            color = TripFlowColors.TextPrimary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(TripFlowColors.Border),
        )
    }
}

// Una tappa della timeline: orario a sinistra, linea verticale con pallino, scheda.
@Composable
internal fun TimelineStop(
    stage: StageDetail,
    isFirst: Boolean,
    isLast: Boolean,
    canEdit: Boolean,
    isDeleting: Boolean,
    onDelete: () -> Unit,
    onAddToCalendar: () -> Unit,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)) {
        Text(
            text = stage.startTime,
            style = MaterialTheme.typography.labelMedium,
            color = TripFlowColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(44.dp)
                .padding(top = 16.dp),
        )
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            // Linea continua tra le tappe dello stesso giorno: sopra il primo pallino
            // e sotto l'ultimo non si disegna.
            Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(18.dp)
                        .background(if (isFirst) TripFlowColors.Background else TripFlowColors.AccentBorder),
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (isLast) TripFlowColors.Background else TripFlowColors.AccentBorder),
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(14.dp)
                    .background(TripFlowColors.AccentBorder, CircleShape)
                    .padding(3.dp)
                    .background(TripFlowColors.Accent, CircleShape),
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else Dimens.gapS),
        ) {
            StageCard(
                stage = stage,
                canEdit = canEdit,
                isDeleting = isDeleting,
                onDelete = onDelete,
                onAddToCalendar = onAddToCalendar,
            )
        }
    }
}

@Composable
private fun StageCard(
    stage: StageDetail,
    canEdit: Boolean,
    isDeleting: Boolean,
    onDelete: () -> Unit,
    onAddToCalendar: () -> Unit,
) {
    Surface(
        color = TripFlowColors.Background,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, TripFlowColors.Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = stage.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TripFlowColors.TextPrimary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = listOf(stage.startTime, stage.endTime).filter { it.isNotBlank() }.joinToString(" – "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TripFlowColors.TextSecondary,
                    )
                    if (stage.stopType != StopType.CUSTOM) {
                        TypeTag(if (stage.stopType == StopType.TRIP) "Viaggio" else "Attività")
                    }
                }
                if (!stage.location.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TripFlowColors.TextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stage.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = TripFlowColors.TextSecondary,
                        )
                    }
                }
                stage.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TripFlowColors.TextBody,
                    )
                }
                // Note personali della tappa, in un riquadro chiaro per distinguerle dalla descrizione.
                stage.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Surface(
                        color = TripFlowColors.Surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = "Note",
                                tint = TripFlowColors.Accent,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 1.dp),
                            )
                            Text(
                                text = notes.trim(),
                                style = MaterialTheme.typography.bodySmall,
                                color = TripFlowColors.TextBody,
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onAddToCalendar) {
                Surface(
                    color = TripFlowColors.Surface2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = "Aggiungi al calendario",
                            tint = TripFlowColors.Accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            if (canEdit) {
                IconButton(onClick = onDelete, enabled = !isDeleting) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = TripFlowColors.Accent,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina tappa",
                            tint = TripFlowColors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeTag(text: String) {
    Surface(color = TripFlowColors.AccentSoft.copy(alpha = 0.45f), shape = RoundedCornerShape(8.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TripFlowColors.AccentDark,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}
