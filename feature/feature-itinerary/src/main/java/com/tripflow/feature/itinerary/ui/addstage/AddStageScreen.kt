package com.tripflow.feature.itinerary.ui.addstage

import com.tripflow.feature.itinerary.ui.common.WithItalianLocale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.runtime.remember
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextArea
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.format.Formatters
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.StopType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStageScreen(
    itineraryId: String,
    viewModel: AddStopViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onStopAdded: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(itineraryId) {
        viewModel.initialize(itineraryId)
    }

    LaunchedEffect(state.addedStageId) {
        if (state.addedStageId != null) onStopAdded()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aggiungi tappa",
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
        when {
            state.isLoadingContext -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TripFlowColors.Accent)
                }
            }

            state.contextError != null -> {
                AddStageContextError(
                    message = requireNotNull(state.contextError),
                    retryable = state.contextErrorRetryable,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onRetry = { viewModel.initialize(itineraryId) },
                )
            }

            else -> {
                AddStageForm(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onStopTypeChange = viewModel::onStopTypeChange,
                    onDateClick = { showDatePicker = true },
                    onStartTimeChange = viewModel::onStartTimeChange,
                    onEndTimeChange = viewModel::onEndTimeChange,
                    onCustomTitleChange = viewModel::onCustomTitleChange,
                    onCustomDescriptionChange = viewModel::onCustomDescriptionChange,
                    onNotesChange = viewModel::onNotesChange,
                    onOrderIndexChange = viewModel::onOrderIndexChange,
                    onCatalogQueryChange = viewModel::onCatalogQueryChange,
                    onSearchCatalog = viewModel::searchCatalog,
                    onSelectCatalogItem = viewModel::selectCatalogItem,
                    onSubmit = viewModel::addStop,
                    onBack = onBack,
                )
            }
        }
    }

    if (showDatePicker) {
        AddStageDatePickerDialog(
            initialDate = state.date,
            minDate = state.itineraryStartDate,
            maxDate = state.itineraryEndDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                viewModel.onDateChange(it)
                showDatePicker = false
            },
        )
    }
}

@Composable
private fun AddStageForm(
    state: AddStopUiState,
    modifier: Modifier,
    onStopTypeChange: (StopType) -> Unit,
    onDateClick: () -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onCustomTitleChange: (String) -> Unit,
    onCustomDescriptionChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onOrderIndexChange: (Int) -> Unit,
    onCatalogQueryChange: (String) -> Unit,
    onSearchCatalog: () -> Unit,
    onSelectCatalogItem: (CatalogItem) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenPadding, vertical = Dimens.gapM),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapL),
    ) {
        Text(
            text = "Scegli il tipo di tappa e quando svolgerla.",
            style = MaterialTheme.typography.bodyMedium,
            color = TripFlowColors.TextSecondary,
        )

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
            Text(
                text = "Tipo di tappa",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextBody,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                StopType.entries.forEach { type ->
                    FilterChip(
                        selected = state.stopType == type,
                        onClick = { onStopTypeChange(type) },
                        label = { Text(type.label()) },
                        enabled = !state.isSubmitting,
                    )
                }
            }
        }

        DateField(
            date = state.date,
            enabled = !state.isSubmitting,
            onClick = onDateClick,
        )
        state.dateError?.let { ErrorText(it) }

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
            TripFlowTextField(
                value = state.startTime,
                onValueChange = onStartTimeChange,
                label = "Inizio",
                placeholder = "HH:mm",
                keyboardType = KeyboardType.Number,
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f),
            )
            TripFlowTextField(
                value = state.endTime,
                onValueChange = onEndTimeChange,
                label = "Fine",
                placeholder = "HH:mm",
                keyboardType = KeyboardType.Number,
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f),
            )
        }
        state.timeError?.let { ErrorText(it) }

        OrderIndexField(
            orderIndex = state.orderIndex,
            enabled = !state.isSubmitting,
            onOrderIndexChange = onOrderIndexChange,
        )

        when (state.stopType) {
            StopType.CUSTOM -> {
                TripFlowTextField(
                    value = state.customTitle,
                    onValueChange = onCustomTitleChange,
                    label = "Titolo",
                    placeholder = "Es. Colazione al mercato",
                    error = state.customTitleError,
                    enabled = !state.isSubmitting,
                )
                TripFlowTextArea(
                    value = state.customDescription,
                    onValueChange = onCustomDescriptionChange,
                    label = "Descrizione (opzionale)",
                    maxChars = 150,
                    placeholder = "Aggiungi qualche dettaglio",
                    minHeight = 96.dp,
                )
            }

            StopType.TRIP,
            StopType.ACTIVITY -> {
                CatalogSearchSection(
                    state = state,
                    onQueryChange = onCatalogQueryChange,
                    onSearch = onSearchCatalog,
                    onSelect = onSelectCatalogItem,
                )
            }
        }

        TripFlowTextArea(
            value = state.notes,
            onValueChange = onNotesChange,
            label = "Note (opzionale)",
            maxChars = 1000,
            placeholder = "Promemoria, prenotazioni, contatti...",
            minHeight = 80.dp,
        )

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

        PrimaryButton(
            text = if (state.isSubmitting) "Aggiunta in corso..." else "Aggiungi tappa",
            onClick = onSubmit,
            enabled = !state.isSubmitting,
        )
        SecondaryButton(
            text = "Annulla",
            onClick = onBack,
            enabled = !state.isSubmitting,
        )
    }
}

@Composable
private fun CatalogSearchSection(
    state: AddStopUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelect: (CatalogItem) -> Unit,
) {
    val catalogLabel = if (state.stopType == StopType.TRIP) "Cerca un viaggio" else "Cerca un'attività"
    TripFlowTextField(
        value = state.catalogQuery,
        onValueChange = onQueryChange,
        label = catalogLabel,
        placeholder = "Scrivi almeno una parola chiave",
        enabled = !state.isSubmitting && !state.isSearchingCatalog,
        trailing = {
            IconButton(
                onClick = onSearch,
                enabled = !state.isSearchingCatalog,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cerca",
                    tint = TripFlowColors.TextSecondary,
                )
            }
        },
    )
    SecondaryButton(
        text = if (state.isSearchingCatalog) "Ricerca in corso..." else "Cerca nel catalogo",
        onClick = onSearch,
        enabled = !state.isSubmitting && !state.isSearchingCatalog,
    )

    state.selectedCatalogItem?.let { selected ->
        Surface(
            color = TripFlowColors.AccentSoft,
            shape = RoundedCornerShape(Dimens.radiusField),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(Dimens.gapM)) {
                Text(
                    text = "Selezionato",
                    style = MaterialTheme.typography.labelSmall,
                    color = TripFlowColors.Accent,
                )
                Text(
                    text = selected.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TripFlowColors.TextPrimary,
                )
            }
        }
    }

    state.catalogResults.forEach { item ->
        CatalogItemRow(
            item = item,
            selected = state.selectedCatalogItem?.id == item.id,
            onClick = { onSelect(item) },
        )
    }

    state.catalogError?.let { message ->
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (state.selectedCatalogItem == null && state.catalogResults.isEmpty()) {
                TripFlowColors.FieldError
            } else {
                TripFlowColors.TextSecondary
            },
        )
    }
}

@Composable
private fun CatalogItemRow(
    item: CatalogItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) TripFlowColors.AccentSoft else TripFlowColors.Background,
        shape = RoundedCornerShape(Dimens.radiusField),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) TripFlowColors.Accent else TripFlowColors.Border,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.gapM),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = TripFlowColors.TextPrimary,
            )
            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (item.location.isNotBlank()) {
                Text(
                    text = item.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun OrderIndexField(
    orderIndex: Int,
    enabled: Boolean,
    onOrderIndexChange: (Int) -> Unit,
) {
    Surface(
        color = TripFlowColors.Surface2,
        shape = RoundedCornerShape(Dimens.radiusField),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.gapM, vertical = Dimens.gapS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapS),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Posizione",
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextBody,
                )
                Text(
                    text = "Tappa $orderIndex",
                    style = MaterialTheme.typography.titleMedium,
                    color = TripFlowColors.TextPrimary,
                )
            }
            IconButton(
                onClick = { onOrderIndexChange(orderIndex - 1) },
                enabled = enabled && orderIndex > 1,
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Sposta prima")
            }
            IconButton(
                onClick = { onOrderIndexChange(orderIndex + 1) },
                enabled = enabled,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Sposta dopo")
            }
        }
    }
}

@Composable
private fun DateField(
    date: LocalDate?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
        Text(
            text = "Data",
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

@Composable
private fun AddStageContextError(
    message: String,
    retryable: Boolean,
    modifier: Modifier,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier.padding(Dimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Impossibile caricare l'itinerario",
            style = MaterialTheme.typography.titleLarge,
            color = TripFlowColors.TextPrimary,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TripFlowColors.TextSecondary,
            modifier = Modifier.padding(top = Dimens.gapS),
        )
        if (retryable) {
            SecondaryButton(
                text = "Riprova",
                onClick = onRetry,
                modifier = Modifier.padding(top = Dimens.gapXL),
            )
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = TripFlowColors.FieldError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStageDatePickerDialog(
    initialDate: LocalDate?,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    // Lo stato va creato dentro WithItalianLocale: il DatePicker fissa la lingua alla creazione.
    WithItalianLocale {
        // Solo i giorni dell'itinerario sono selezionabili (il backend rifiuta gli altri).
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate?.toUtcMillis(),
            initialDisplayedMonthMillis = (initialDate ?: minDate)?.toUtcMillis(),
            selectableDates = remember(minDate, maxDate) { ItineraryDates(minDate, maxDate) },
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

@OptIn(ExperimentalMaterial3Api::class)
private class ItineraryDates(
    private val minDate: LocalDate?,
    private val maxDate: LocalDate?,
) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val date = utcTimeMillis.toLocalDateUtc()
        return (minDate == null || !date.isBefore(minDate)) && (maxDate == null || !date.isAfter(maxDate))
    }

    override fun isSelectableYear(year: Int): Boolean =
        (minDate == null || year >= minDate.year) && (maxDate == null || year <= maxDate.year)
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun StopType.label(): String = when (this) {
    StopType.TRIP -> "Viaggio"
    StopType.ACTIVITY -> "Attività"
    StopType.CUSTOM -> "Personalizzata"
}
