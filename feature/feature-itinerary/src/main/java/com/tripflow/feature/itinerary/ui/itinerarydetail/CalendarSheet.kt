package com.tripflow.feature.itinerary.ui.itinerarydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.itinerary.model.StageDetail
import java.time.format.DateTimeFormatter
import java.util.Locale

private val sheetDateFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ITALIAN)

// Pannello "Aggiungi al calendario": scelta delle tappe (tutte selezionate all'inizio)
// e aggiunta in un colpo solo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarSheet(
    stages: List<StageDetail>,
    isAdding: Boolean,
    onAdd: (List<StageDetail>) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedIds = remember(stages) { mutableStateListOf<String>().apply { addAll(stages.map { it.id }) } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TripFlowColors.Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.screenPadding, end = Dimens.screenPadding, bottom = Dimens.gapXL),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapS),
        ) {
            Text(
                text = "Aggiungi al calendario",
                style = MaterialTheme.typography.titleLarge,
                color = TripFlowColors.TextPrimary,
            )
            Text(
                text = "Le tappe scelte diventano eventi nel calendario del telefono, con orari e note.",
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextBody,
            )

            Column(
                modifier = Modifier
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapS),
            ) {
                stages.forEach { stage ->
                    val checked = stage.id in selectedIds
                    Surface(
                        color = TripFlowColors.Background,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TripFlowColors.Border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (checked) selectedIds.remove(stage.id) else selectedIds.add(stage.id)
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(end = 12.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    if (it) selectedIds.add(stage.id) else selectedIds.remove(stage.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = TripFlowColors.Accent),
                            )
                            Column {
                                Text(
                                    text = stage.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TripFlowColors.TextPrimary,
                                )
                                Text(
                                    text = buildString {
                                        append(stage.date.format(sheetDateFormatter))
                                        val times = listOf(stage.startTime, stage.endTime).filter { it.isNotBlank() }
                                        if (times.isNotEmpty()) append(" · ").append(times.joinToString(" – "))
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TripFlowColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            val count = selectedIds.size
            PrimaryButton(
                text = when {
                    isAdding -> "Aggiunta in corso…"
                    count == 1 -> "Aggiungi 1 evento"
                    else -> "Aggiungi $count eventi"
                },
                onClick = { onAdd(stages.filter { it.id in selectedIds }) },
                enabled = count > 0 && !isAdding,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.gapS),
            )
        }
    }
}
