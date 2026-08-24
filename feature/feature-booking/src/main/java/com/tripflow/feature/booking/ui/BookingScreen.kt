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
import com.tripflow.core.ui.component.PrimaryButton
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
    onBack: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    viewModel: BookingScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            BookingBottomBar(
                totalPrice = uiState.totalPrice,
                onPaymentClick = onPaymentClick
            )
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapXL)
        ) {
            TripSummaryHeader()

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
                            "Massimo 12 posti disponibili",
                            style = MaterialTheme.typography.bodySmall,
                            color = TripFlowColors.TextSecondary
                        )
                    }
                    Stepper(
                        value = uiState.participants,
                        onValueChange = { viewModel.onParticipantsChange(it) },
                        minValue = 1,
                        maxValue = 12
                    )
                }
            }

            Section(title = "ATTIVITÀ OPZIONALI") {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
                    uiState.activities.forEach { activity ->
                        ActivityItem(
                            activity = activity,
                            onToggle = {
                                viewModel.onToggleActivity(activity.name)
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

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    TripFlowTheme {
        BookingScreen()
    }
}
