package com.tripflow.feature.booking

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
import coil.compose.AsyncImage
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.TripFlowTextArea
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onBack: () -> Unit = {},
    onPaymentClick: () -> Unit = {}
) {
    var participants by remember { mutableStateOf(2) }
    var notes by remember { mutableStateOf("") }
    val activities = remember {
        mutableStateListOf(
            ActivityUi("Tour in barca a Capri", 45, true, "4h"),
            ActivityUi("Degustazione di limoncello", 30, false, "1h 30m"),
            ActivityUi("Sentiero degli Dei", 25, false, "5h")
        )
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
            BookingBottomBar(
                totalPrice = 480 * participants + activities.filter { it.isSelected }.sumOf { it.price * participants },
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
                        value = participants,
                        onValueChange = { participants = it },
                        minValue = 1,
                        maxValue = 12
                    )
                }
            }

            Section(title = "ATTIVITÀ OPZIONALI") {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
                    activities.forEachIndexed { index, activity ->
                        ActivityItem(
                            activity = activity,
                            onToggle = { activities[index] = activity.copy(isSelected = !activity.isSelected) }
                        )
                    }
                }
            }

            Section(title = "NOTE PER L'ORGANIZZATORE") {
                TripFlowTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "",
                    placeholder = "Allergie, richieste particolari, orario di arrivo...",
                    maxChars = 1000
                )
            }

            Section(title = "RIEPILOGO") {
                SummaryRow("Viaggio € 480 x $participants", "€ ${480 * participants}")
                activities.filter { it.isSelected }.forEach { activity ->
                    SummaryRow("${activity.name} € ${activity.price} x $participants", "€ ${activity.price * participants}")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.gapS), color = TripFlowColors.Border)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Totale", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "€ ${480 * participants + activities.filter { it.isSelected }.sumOf { it.price * participants }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TripFlowColors.TextSecondary,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
        )
        content()
    }
}

@Composable
fun TripSummaryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(TripFlowColors.Surface)
            .padding(Dimens.gapM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        AsyncImage( //immagine a lato, da rivedere. Nota: da considerare sfondo del viaggio nell'header
            model = null, //mock per ora
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(Dimens.radiusChip))
                .background(TripFlowColors.Surface2),
            contentScale = ContentScale.Crop
        )
        Column {
            Text("Costa Amalfitana", style = MaterialTheme.typography.titleMedium)
            Text("14 – 18 set 2026 · Amalfi", style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
        }
    }
}

@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 1,
    maxValue: Int = 99
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapL)
    ) {
        IconButton(
            onClick = { if (value > minValue) onValueChange(value - 1) },
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TripFlowColors.Surface2)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Rimuovi", modifier = Modifier.size(16.dp))
        }
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        IconButton(
            onClick = { if (value < maxValue) onValueChange(value + 1) },
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TripFlowColors.TextPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Aggiungi", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

data class ActivityUi(val name: String, val price: Int, val isSelected: Boolean, val duration: String)

@Composable
fun ActivityItem(activity: ActivityUi, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = if (activity.isSelected) 1.5.dp else 1.dp,
                color = if (activity.isSelected) TripFlowColors.Accent else TripFlowColors.Border,
                shape = shape
            )
            .background(if (activity.isSelected) TripFlowColors.AccentSoft else Color.Transparent)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = activity.isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = TripFlowColors.Accent)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.name, style = MaterialTheme.typography.titleSmall)
            Text(activity.duration, style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("€ ${activity.price}", style = MaterialTheme.typography.titleSmall)
            Text("a persona", style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TripFlowColors.TextBody)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TripFlowColors.TextPrimary)
    }
}

@Composable
fun BookingBottomBar(totalPrice: Int, onPaymentClick: () -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        color = TripFlowColors.Background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenPadding, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapL)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("totale", style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
                Text("€ $totalPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            PrimaryButton(
                text = "Vai al pagamento",
                onClick = onPaymentClick,
                modifier = Modifier.weight(1.5f)
            )
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
