package com.tripflow.feature.itinerary.ui.itinerarydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.itinerary.model.ItineraryShare

// Finestra "Condividi con un utente": elenco delle condivisioni attive (con rimozione)
// e aggiunta tramite il codice utente del destinatario.
@Composable
internal fun ShareDialog(
    state: ShareUiState,
    onShare: (code: String, canEdit: Boolean) -> Unit,
    onRemove: (userId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var canEdit by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Condividi con un utente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                when {
                    state.isLoading -> CircularProgressIndicator(
                        color = TripFlowColors.Accent,
                        modifier = Modifier.size(24.dp),
                    )
                    state.shares.isEmpty() -> Text(
                        text = "Non hai ancora condiviso questo itinerario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TripFlowColors.TextSecondary,
                    )
                    else -> state.shares.forEach { share ->
                        ShareRow(share = share, onRemove = { onRemove(share.userId) })
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.gapS))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Codice utente") },
                    supportingText = { Text("Il destinatario lo trova in Itinerari, icona in alto.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Può modificare", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = canEdit, onCheckedChange = { canEdit = it })
                }
                state.error?.let {
                    Text(text = it, color = TripFlowColors.Error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onShare(code, canEdit)
                    code = ""
                },
                enabled = code.isNotBlank() && !state.isSubmitting,
            ) {
                Text(if (state.isSubmitting) "Condivisione…" else "Condividi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Chiudi") }
        },
        containerColor = TripFlowColors.Background,
        titleContentColor = TripFlowColors.TextPrimary,
        textContentColor = TripFlowColors.TextBody,
    )
}

@Composable
private fun ShareRow(share: ItineraryShare, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = share.displayName ?: share.userId.take(8) + "…",
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextPrimary,
            )
            Text(
                text = if (share.canEdit) "Può modificare" else "Sola lettura",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextSecondary,
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "Rimuovi condivisione")
        }
    }
}
