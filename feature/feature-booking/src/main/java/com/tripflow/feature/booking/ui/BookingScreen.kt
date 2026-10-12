package com.tripflow.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripflow.core.model.UiState
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.component.TripFlowTextArea
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.booking.ui.components.ActivityItem
import com.tripflow.feature.booking.ui.components.BookingBottomBar
import com.tripflow.feature.booking.ui.components.Section
import com.tripflow.feature.booking.ui.components.Stepper
import com.tripflow.feature.booking.ui.components.SummaryRow
import com.tripflow.feature.booking.ui.components.TripSummaryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viaggioId: String? = null,
    onBack: () -> Unit = {},
    onPaymentClick: (String) -> Unit = {},
    viewModel: BookingScreenViewModel = viewModel(key = viaggioId) { BookingScreenViewModel(viaggioId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdBookingId) {
        uiState.createdBookingId?.let { id ->
            onPaymentClick(id)
            viewModel.onNavigatedToPayment()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prenota", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TripFlowColors.Background
                )
            )
        },
        bottomBar = {
            if (uiState.trip is UiState.Success) {
                BookingBottomBar(
                    totalPrice = uiState.totalPrice,
                    onPaymentClick = viewModel::onConfirm,
                    isSubmitting = uiState.isSubmitting,
                    error = uiState.submitError
                )
            }
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        StateHost(
            state = uiState.trip,
            onRetry = viewModel::loadTrip,
            modifier = Modifier.padding(innerPadding)
        ) { trip ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapXL)
            ) {
                TripSummaryHeader(trip)

                Section(title = "PARTECIPANTI") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Quante persone?",
                                style = MaterialTheme.typography.titleMedium,
                                color = TripFlowColors.TextPrimary
                            )
                            Text(
                                if (uiState.maxParticipants > 0) "Massimo ${uiState.maxParticipants} posti disponibili" else "Posti esauriti",
                                style = MaterialTheme.typography.bodySmall,
                                color = TripFlowColors.TextSecondary
                            )
                        }
                        Stepper(
                            value = uiState.participants,
                            onValueChange = { viewModel.onParticipantsChange(it) },
                            minValue = 1,
                            maxValue = uiState.maxParticipants.coerceAtLeast(1)
                        )
                    }
                }

                Section(title = "ATTIVITÀ OPZIONALI") {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
                        if (uiState.activities.isEmpty()) {
                            Text(
                                "Nessuna attività opzionale per questo viaggio",
                                style = MaterialTheme.typography.bodySmall,
                                color = TripFlowColors.TextSecondary
                            )
                        }
                        uiState.activities.forEach { activity ->
                            ActivityItem(
                                activity = activity,
                                onToggle = {
                                    viewModel.onToggleActivity(activity.id)
                                }
                            )
                        }
                    }
                }

                Section(title = "NOTE PER L'ORGANIZZATORE") {
                    TripFlowTextArea(
                        value = uiState.notes,
                        onValueChange = { viewModel.onNotesChange(it) },
                        label = "",
                        placeholder = "Allergie, richieste particolari, orario di arrivo...",
                        maxChars = 1000
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.gapM))

                Section(title = "RIEPILOGO") {
                    SummaryRow(
                        "Viaggio € ${uiState.basePricePerPerson} x ${uiState.participants}",
                        "€ ${uiState.basePricePerPerson * uiState.participants}"
                    )
                    uiState.activities.filter { it.isSelected }.forEach { activity ->
                        SummaryRow(
                            "${activity.name} € ${activity.price} x ${uiState.participants}",
                            "€ ${activity.price * uiState.participants}"
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Dimens.gapL),
                        color = TripFlowColors.Border
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Totale",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "€ ${uiState.totalPrice}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    TripFlowTheme {
        BookingScreen()
    }
}
