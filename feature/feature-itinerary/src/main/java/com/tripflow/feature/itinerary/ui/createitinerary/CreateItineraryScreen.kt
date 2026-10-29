package com.tripflow.feature.itinerary.ui.createitinerary

import com.tripflow.feature.itinerary.ui.common.WithItalianLocale
import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextArea
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.format.Formatters
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.itinerary.model.Visibility
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItineraryScreen(
    viewModel: CreateItineraryViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCreated: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var dateTarget by rememberSaveable { mutableStateOf<DateTarget?>(null) }

    LaunchedEffect(state.createdItineraryId) {
        state.createdItineraryId?.let(onCreated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuovo itinerario",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TripFlowColors.TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !state.isSubmitting) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = TripFlowColors.TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TripFlowColors.Background,
                    titleContentColor = TripFlowColors.TextPrimary,
                ),
            )
        },
        containerColor = TripFlowColors.Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.gapM),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapL),
        ) {
            Text(
                text = "Dai un nome al viaggio e scegli quando partire.",
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary,
            )

            TripFlowTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "Titolo",
                placeholder = "Es. Weekend a Roma",
                error = state.titleError,
                enabled = !state.isSubmitting,
            )

            TripFlowTextArea(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Descrizione (opzionale)",
                maxChars = 1000,
                placeholder = "Racconta in breve il tuo viaggio",
                minHeight = 108.dp,
            )

            DateField(
                label = "Data di inizio",
                date = state.startDate,
                enabled = !state.isSubmitting,
                onClick = { dateTarget = DateTarget.START },
            )

            DateField(
                label = "Data di fine",
                date = state.endDate,
                enabled = !state.isSubmitting,
                onClick = { dateTarget = DateTarget.END },
            )

            state.dateError?.let { dateError ->
                Text(
                    text = dateError,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.FieldError,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                Text(
                    text = "Visibilità",
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextBody,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                    // "Condiviso" non si sceglie in creazione: l'itinerario lo diventa da solo
                    // quando lo condividi con qualcuno dal dettaglio. Cosi' non esistono
                    // itinerari "condivisi con nessuno".
                    listOf(Visibility.PRIVATE, Visibility.PUBLIC).forEach { visibility ->
                        FilterChip(
                            selected = state.visibility == visibility,
                            onClick = { viewModel.onVisibilityChange(visibility) },
                            label = { Text(visibility.label()) },
                            enabled = !state.isSubmitting,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TripFlowColors.AccentSoft.copy(alpha = 0.5f),
                                selectedLabelColor = TripFlowColors.AccentDark,
                            ),
                        )
                    }
                }
                Text(
                    text = state.visibility.description(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary,
                )
            }

            state.errorMessage?.let { errorMessage ->
                Surface(
                    color = TripFlowColors.ErrorSoft,
                    shape = RoundedCornerShape(Dimens.radiusField),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TripFlowColors.Error,
                        modifier = Modifier.padding(Dimens.gapM),
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            PrimaryButton(
                text = if (state.isSubmitting) "Creazione in corso..." else "Crea itinerario",
                onClick = viewModel::createItinerary,
                enabled = !state.isSubmitting,
            )

            SecondaryButton(
                text = "Annulla",
                onClick = onBack,
                enabled = !state.isSubmitting,
            )
        }
    }

    when (dateTarget) {
        DateTarget.START -> ItineraryDatePickerDialog(
            initialDate = state.startDate,
            onDismiss = { dateTarget = null },
            onDateSelected = {
                viewModel.onStartDateChange(it)
                dateTarget = null
            },
        )

        DateTarget.END -> ItineraryDatePickerDialog(
            initialDate = state.endDate,
            onDismiss = { dateTarget = null },
            onDateSelected = {
                viewModel.onEndDateChange(it)
                dateTarget = null
            },
        )

        null -> Unit
    }
}

@Composable
private fun DateField(
    label: String,
    date: LocalDate?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TripFlowColors.TextBody,
        )
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.fieldHeight),
            shape = RoundedCornerShape(Dimens.radiusField),
            border = BorderStroke(
                width = if (date != null) 1.5.dp else 1.dp,
                color = if (date != null) TripFlowColors.Accent else TripFlowColors.Border,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.gapS),
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (date != null) TripFlowColors.Accent else TripFlowColors.TextSecondary,
                )
                Text(
                    text = date?.let(Formatters::date) ?: "Seleziona una data",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (date != null) TripFlowColors.TextPrimary else TripFlowColors.TextSecondary,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryDatePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    // Lo stato va creato dentro WithItalianLocale: il DatePicker fissa la lingua alla creazione.
    WithItalianLocale {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate?.toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.toLocalDateUtc()
                            ?.let(onDateSelected)
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Seleziona la data",
                        modifier = androidx.compose.ui.Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    )
                },
            )
        }
    }
}

private enum class DateTarget {
    START,
    END,
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun Visibility.label(): String = when (this) {
    Visibility.PUBLIC -> "Pubblico"
    Visibility.PRIVATE -> "Privato"
    Visibility.SHARED -> "Condiviso"
}

private fun Visibility.description(): String = when (this) {
    Visibility.PUBLIC -> "Visibile a tutti gli utenti."
    Visibility.PRIVATE -> "Visibile solo a te. Potrai condividerlo con altre persone dal dettaglio."
    Visibility.SHARED -> "Visibile solo alle persone con cui lo condividi."
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun CreateItineraryScreenPreview() {
    TripFlowTheme {
        CreateItineraryScreen(
            viewModel = CreateItineraryViewModel(FakeItineraryRepository()),
            onBack = {},
            onCreated = {},
        )
    }
}
