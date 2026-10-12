package com.tripflow.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.ui.component.InfoChip
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.booking.model.StatoPrenotazione
import com.tripflow.feature.booking.ui.components.ReceivedBookingCard

private val filtriStato = listOf(
    null to "Tutte",
    StatoPrenotazione.IN_ATTESA to "In attesa",
    StatoPrenotazione.CONFERMATA to "Confermate",
    StatoPrenotazione.COMPLETATA to "Completate",
    StatoPrenotazione.ANNULLATA to "Annullate"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivedBookingsScreen(
    onBack: () -> Unit = {},
    onBookingClick: (String) -> Unit = {},
    viewModel: ReceivedBookingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prenotazioni ricevute", style = MaterialTheme.typography.titleLarge) },
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
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val summary = uiState.summary
            if (summary != null) {
                SummaryCard(summary)
                FilterRow(
                    options = filtriStato,
                    selected = uiState.selectedStatus,
                    onSelected = viewModel::onStatusSelected
                )
                if (uiState.trips.size > 1) {
                    FilterRow(
                        options = listOf<Pair<String?, String>>(null to "Tutti i viaggi") +
                            uiState.trips.map { it.id to it.title },
                        selected = uiState.selectedTripId,
                        onSelected = viewModel::onTripSelected
                    )
                }
            }

            StateHost(
                state = uiState.bookings,
                onRetry = viewModel::loadBookings,
                emptyTitle = "Nessuna prenotazione"
            ) { bookings ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
                ) {
                    items(bookings, key = { it.id }) { booking ->
                        ReceivedBookingCard(
                            booking = booking,
                            onClick = { onBookingClick(booking.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: ReceivedSummaryUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenPadding, vertical = Dimens.gapS)
            .background(TripFlowColors.AccentSoft, RoundedCornerShape(Dimens.radiusCard))
            .padding(Dimens.gapL),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryValue(label = "Confermate", value = summary.confirmedBookings.toString())
        SummaryValue(label = "Partecipanti", value = summary.confirmedParticipants.toString())
        SummaryValue(label = "Incasso", value = summary.revenue)
    }
}

@Composable
private fun SummaryValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TripFlowColors.AccentDark
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
    }
}

@Composable
private fun <T> FilterRow(
    options: List<Pair<T?, String>>,
    selected: T?,
    onSelected: (T?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapS),
        contentPadding = PaddingValues(horizontal = Dimens.screenPadding, vertical = Dimens.gapS)
    ) {
        items(options) { (value, label) ->
            InfoChip(
                text = label,
                highlighted = value == selected,
                modifier = Modifier.clickable { onSelected(value) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceivedBookingsScreenPreview() {
    TripFlowTheme {
        ReceivedBookingsScreen()
    }
}
