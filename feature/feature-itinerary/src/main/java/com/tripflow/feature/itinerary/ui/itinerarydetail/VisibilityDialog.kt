package com.tripflow.feature.itinerary.ui.itinerarydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.itinerary.model.Visibility

private data class VisibilityOption(
    val visibility: Visibility,
    val title: String,
    val description: String,
    val icon: ImageVector,
)

private val options = listOf(
    VisibilityOption(Visibility.PRIVATE, "Privato", "Lo vedi solo tu.", Icons.Filled.Lock),
    VisibilityOption(
        Visibility.SHARED, "Condiviso",
        "Lo vedono solo le persone che scegli tu, in sola lettura o con permesso di modifica.",
        Icons.Filled.Group,
    ),
    VisibilityOption(Visibility.PUBLIC, "Pubblico", "Visibile a tutti gli utenti di TripFlow.", Icons.Filled.Public),
)

// Pop-up "Cambia visibilita'": le tre opzioni come schede selezionabili,
// con quella attuale gia' scelta. Il salvataggio e' gestito dal chiamante.
@Composable
internal fun VisibilityDialog(
    current: Visibility,
    isSaving: Boolean,
    onConfirm: (Visibility) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember(current) { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Cambia visibilità") },
        text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapS),
            ) {
                options.forEach { option ->
                    OptionCard(
                        option = option,
                        selected = selected == option.visibility,
                        isCurrent = current == option.visibility,
                        onSelect = { selected = option.visibility },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selected) },
                enabled = !isSaving && selected != current,
            ) {
                Text(
                    text = when {
                        isSaving -> "Salvataggio…"
                        selected == Visibility.SHARED && current != Visibility.SHARED -> "Scegli persone"
                        else -> "Salva"
                    },
                    color = TripFlowColors.Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Annulla") }
        },
        containerColor = TripFlowColors.Background,
        titleContentColor = TripFlowColors.TextPrimary,
        textContentColor = TripFlowColors.TextBody,
    )
}

@Composable
private fun OptionCard(
    option: VisibilityOption,
    selected: Boolean,
    isCurrent: Boolean,
    onSelect: () -> Unit,
) {
    Surface(
        color = if (selected) TripFlowColors.AccentSoft.copy(alpha = 0.25f) else TripFlowColors.Background,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) TripFlowColors.Accent else TripFlowColors.Border,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapM),
        ) {
            Surface(
                color = TripFlowColors.Surface2,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = TripFlowColors.Accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TripFlowColors.TextPrimary,
                    )
                    if (isCurrent) {
                        Text(
                            text = "attuale",
                            style = MaterialTheme.typography.labelSmall,
                            color = TripFlowColors.TextSecondary,
                        )
                    }
                }
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary,
                )
            }
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = TripFlowColors.Accent),
            )
        }
    }
}
