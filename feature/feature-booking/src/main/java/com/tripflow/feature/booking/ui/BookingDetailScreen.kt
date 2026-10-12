package com.tripflow.feature.booking.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.model.UiState
import com.tripflow.core.ui.component.DangerButton
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.component.Status
import com.tripflow.core.ui.component.StatusChip
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.booking.ui.components.CountdownBanner
import com.tripflow.feature.booking.ui.components.Section
import com.tripflow.feature.booking.ui.components.SummaryRow
import com.tripflow.feature.booking.ui.components.TripSummaryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    prenotazioneId: String? = null,
    onBack: () -> Unit = {},
    viewModel: BookingDetailViewModel = viewModel(key = prenotazioneId) { BookingDetailViewModel(prenotazioneId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBooking()
    }

    if (uiState.showCancelDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelDismiss,
            title = { Text("Annullare la prenotazione?") },
            text = { Text("I posti bloccati verranno liberati. L'operazione non si può annullare.") },
            confirmButton = {
                TextButton(onClick = viewModel::onCancelConfirm) {
                    Text("Annulla prenotazione", color = TripFlowColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelDismiss) {
                    Text("Indietro")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prenotazione", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TripFlowColors.Background
                )
            )
        },
        bottomBar = {
            val booking = (uiState.booking as? UiState.Success)?.data
            if (booking != null && (booking.canPay || booking.canCancel || uiState.isPaying)) {
                DetailActions(
                    booking = booking,
                    isPaying = uiState.isPaying,
                    isCancelling = uiState.isCancelling,
                    error = uiState.actionError,
                    onPayClick = viewModel::onPayClick,
                    onCancelClick = viewModel::onCancelClick
                )
            }
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        StateHost(
            state = uiState.booking,
            onRetry = viewModel::loadBooking,
            modifier = Modifier.padding(innerPadding)
        ) { booking ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapXL)
            ) {
                TripSummaryHeader(booking.trip)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stato", style = MaterialTheme.typography.titleMedium)
                    StatusChip(style = Status.prenotazione(booking.status))
                }

                uiState.secondsLeft?.let { CountdownBanner(it) }

                Section(title = "ATTIVITÀ") {
                    if (booking.activities.isEmpty()) {
                        Text(
                            "Nessuna attività aggiunta",
                            style = MaterialTheme.typography.bodySmall,
                            color = TripFlowColors.TextSecondary
                        )
                    }
                    booking.activities.forEach { activity ->
                        SummaryRow("${activity.name} · ${activity.duration}", "€ ${activity.price} a persona")
                    }
                }

                booking.notes?.let { notes ->
                    Section(title = "NOTE PER L'ORGANIZZATORE") {
                        Text(notes, style = MaterialTheme.typography.bodyMedium, color = TripFlowColors.TextBody)
                    }
                }

                booking.paymentStatus?.let { paymentStatus ->
                    Section(title = "PAGAMENTO") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                booking.paymentDetail.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TripFlowColors.TextBody,
                                modifier = Modifier.weight(1f)
                            )
                            StatusChip(style = Status.pagamento(paymentStatus))
                        }
                    }
                }

                Section(title = "RIEPILOGO") {
                    SummaryRow("Prezzo viaggio a persona", booking.unitPrice)
                    SummaryRow("Partecipanti", booking.participants.toString())
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Dimens.gapL),
                        color = TripFlowColors.Border
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Totale", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(booking.totalPrice, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DetailActions(
    booking: BookingDetailUi,
    isPaying: Boolean,
    isCancelling: Boolean,
    error: String?,
    onPayClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = TripFlowColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            if (error != null) {
                Text(error, style = MaterialTheme.typography.bodySmall, color = TripFlowColors.Error)
            }
            if (booking.canPay || isPaying) {
                PrimaryButton(
                    text = if (isPaying) "Pagamento in corso..." else "Paga ${booking.totalPrice}",
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPaying && !isCancelling
                )
            }
            if (booking.canCancel) {
                DangerButton(
                    text = if (isCancelling) "Annullamento in corso..." else "Annulla prenotazione",
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPaying && !isCancelling
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingDetailScreenPreview() {
    TripFlowTheme {
        BookingDetailScreen()
    }
}
