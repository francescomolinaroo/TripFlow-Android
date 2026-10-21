package com.tripflow.feature.catalog.presentation.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.TripFlowTextArea
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val ISO = DateTimeFormatter.ISO_LOCAL_DATE

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun CreateTripScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateTripViewModel = viewModel()
) {
    val form by viewModel.form.collectAsState()

    // Redirect to discover when trip is successfully created
    LaunchedEffect(form.successMessage) {
        if (form.successMessage != null) {
            // success dialog will handle navigation via its button
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TripFlowColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Top bar ──────────────────────────────────────────────────
            CreateTripTopBar(onBack = onBack)

            HorizontalDivider(color = TripFlowColors.Divider)

            // ── Scrollable form ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.screenPadding)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapL)
            ) {
                Spacer(modifier = Modifier.height(Dimens.gapM))

                // Section: Informazioni base
                SectionLabel("Informazioni base")

                TripFlowTextField(
                    value = form.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nome del viaggio *",
                    placeholder = "es. Avventura nelle Dolomiti",
                    error = form.nameError,
                    enabled = !form.isLoading,
                    isPassword = false,
                    leading = { FieldIcon(Icons.Default.Title) }
                )

                TripFlowTextField(
                    value = form.destination,
                    onValueChange = viewModel::onDestinationChange,
                    label = "Destinazione *",
                    placeholder = "es. Trento, Italia",
                    error = form.destinationError,
                    enabled = !form.isLoading,
                    isPassword = false,
                    leading = { FieldIcon(Icons.Default.LocationOn) }
                )

                // Section: Date
                SectionLabel("Date del viaggio")

                DateField(
                    value = form.startDate,
                    onValueChange = viewModel::onStartDateChange,
                    label = "Data di inizio *",
                    error = form.startDateError,
                    enabled = !form.isLoading,
                    isStartDate = true
                )

                DateField(
                    value = form.endDate,
                    onValueChange = viewModel::onEndDateChange,
                    label = "Data di fine *",
                    error = form.endDateError,
                    enabled = !form.isLoading,
                    isStartDate = false,
                    minDateMillis = if (form.startDate.isNotBlank()) {
                        runCatching {
                            LocalDate.parse(form.startDate, ISO)
                                .plusDays(1)
                                .atStartOfDay(ZoneOffset.UTC)
                                .toInstant()
                                .toEpochMilli()
                        }.getOrNull()
                    } else null
                )

                // Section: Prezzi e posti
                SectionLabel("Prezzi e disponibilità")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
                ) {
                    TripFlowTextField(
                        value = form.price,
                        onValueChange = viewModel::onPriceChange,
                        label = "Prezzo (€) *",
                        placeholder = "0.00",
                        error = form.priceError,
                        enabled = !form.isLoading,
                        isPassword = false,
                        keyboardType = KeyboardType.Decimal,
                        leading = { FieldIcon(Icons.Default.Euro) },
                        modifier = Modifier.weight(1f)
                    )
                    TripFlowTextField(
                        value = form.availableSpots,
                        onValueChange = viewModel::onAvailableSpotsChange,
                        label = "Posti disponibili *",
                        placeholder = "0",
                        error = form.availableSpotsError,
                        enabled = !form.isLoading,
                        isPassword = false,
                        keyboardType = KeyboardType.Number,
                        leading = { FieldIcon(Icons.Default.Group) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Section: Descrizione
                SectionLabel("Descrizione")

                TripFlowTextArea(
                    value = form.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = "Descrizione del viaggio",
                    placeholder = "Racconta cosa rende speciale questo viaggio…",
                    maxChars = 1000,
                    minHeight = 120.dp
                )

                // Section: Immagini
                SectionLabel("Immagini")

                ImageUrlsField(
                    value = form.imagesRaw,
                    onValueChange = viewModel::onImagesRawChange,
                    enabled = !form.isLoading
                )

                // Global error
                AnimatedVisibility(
                    visible = form.globalError != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    if (form.globalError != null) {
                        ErrorBanner(message = form.globalError!!)
                    }
                }

                // Submit button
                PrimaryButton(
                    text = if (form.isLoading) "Creazione in corso…" else "Crea viaggio",
                    onClick = viewModel::submit,
                    enabled = !form.isLoading
                )

                Spacer(modifier = Modifier.height(Dimens.gapXL))
            }
        }

        // Loading overlay
        if (form.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TripFlowColors.Background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TripFlowColors.Accent)
            }
        }
    }

    // Success dialog
    if (form.successMessage != null) {
        SuccessDialog(
            message = form.successMessage!!,
            onDismiss = {
                viewModel.dismissSuccess()
                onCreated()
            }
        )
    }
}

// ─── Top bar ─────────────────────────────────────────────────────────────────

@Composable
private fun CreateTripTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Indietro",
                tint = TripFlowColors.TextPrimary
            )
        }
        Column {
            Text(
                text = "Crea viaggio",
                style = MaterialTheme.typography.titleLarge,
                color = TripFlowColors.TextPrimary
            )
            Text(
                text = "Compila il form per pubblicare un nuovo viaggio",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextSecondary
            )
        }
    }
}

// ─── Section label ───────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Column {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TripFlowColors.TextSecondary
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 6.dp),
            color = TripFlowColors.Divider
        )
    }
}

// ─── Icon helper ─────────────────────────────────────────────────────────────

@Composable
private fun FieldIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = TripFlowColors.TextSecondary,
        modifier = Modifier.size(18.dp)
    )
}

// ─── Date field with picker ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    isStartDate: Boolean,
    minDateMillis: Long? = null
) {
    var showPicker by remember { mutableStateOf(false) }

    // pre-select the current value in the DatePicker if parseable
    val initialMillis = remember(value) {
        runCatching {
            LocalDate.parse(value, ISO).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
    }

    val selectableDates = remember(minDateMillis) {
        val minMs = minDateMillis
        if (minMs != null) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= minMs
            }
        } else {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
            }
        }
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = selectableDates
    )

    TripFlowTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = "aaaa-mm-gg",
        error = error,
        enabled = enabled,
        isPassword = false,
        keyboardType = KeyboardType.Text,
        leading = { FieldIcon(Icons.Default.CalendarMonth) },
        trailing = {
            IconButton(
                onClick = { if (enabled) showPicker = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Apri calendario",
                    tint = TripFlowColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onValueChange(date.format(ISO))
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Annulla") }
            },
            colors = DatePickerDefaults.colors(
                containerColor = TripFlowColors.Background
            )
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// ─── Image URLs field ─────────────────────────────────────────────────────────

@Composable
private fun ImageUrlsField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TripFlowColors.Border, RoundedCornerShape(Dimens.radiusField))
            .clip(RoundedCornerShape(Dimens.radiusField))
            .background(TripFlowColors.Background)
            .padding(Dimens.gapM),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapS)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapS)
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = TripFlowColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "URL immagini",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextBody
            )
        }
        TripFlowTextArea(
            value = value,
            onValueChange = onValueChange,
            label = "",
            placeholder = "Incolla URL separati da virgola\nhttps://…, https://…",
            maxChars = 2000,
            minHeight = 80.dp
        )
        Text(
            text = "Separa più URL con una virgola. Il primo sarà usato come copertina.",
            style = MaterialTheme.typography.bodySmall,
            color = TripFlowColors.TextSecondary
        )
    }
}

// ─── Error banner ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TripFlowColors.ErrorSoft, RoundedCornerShape(Dimens.radiusField))
            .border(1.dp, TripFlowColors.ErrorBorder, RoundedCornerShape(Dimens.radiusField))
            .padding(horizontal = Dimens.gapM, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FlightTakeoff,
            contentDescription = null,
            tint = TripFlowColors.Error,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TripFlowColors.Error,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─── Success dialog ───────────────────────────────────────────────────────────

@Composable
private fun SuccessDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = TripFlowColors.Success,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Viaggio creato!",
                style = MaterialTheme.typography.titleLarge,
                color = TripFlowColors.TextPrimary
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Vai ai viaggi", color = TripFlowColors.Accent)
            }
        },
        containerColor = TripFlowColors.Background,
        tonalElevation = 0.dp
    )
}
