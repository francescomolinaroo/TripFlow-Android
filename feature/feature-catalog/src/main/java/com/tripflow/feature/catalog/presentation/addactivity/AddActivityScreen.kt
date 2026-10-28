package com.tripflow.feature.catalog.presentation.addactivity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.TripFlowTextArea
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

// ─── Factory ─────────────────────────────────────────────────────────────────

class AddActivityViewModelFactory(private val tripId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AddActivityViewModel(tripId) as T
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun AddActivityScreen(
    tripId: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: AddActivityViewModel = viewModel(
        key = "add_activity_$tripId",
        factory = AddActivityViewModelFactory(tripId)
    )
) {
    val form by viewModel.form.collectAsState()

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
            AddActivityTopBar(onBack = onBack)

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

                // Section: Informazioni
                SectionLabel("Informazioni attività")

                TripFlowTextField(
                    value = form.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nome attività *",
                    placeholder = "es. Tour in bici delle Dolomiti",
                    error = form.nameError,
                    enabled = !form.isLoading,
                    isPassword = false,
                    leading = { FieldIcon(Icons.Default.Title) }
                )

                TripFlowTextArea(
                    value = form.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = "Descrizione",
                    placeholder = "Descrivi brevemente l'attività…",
                    maxChars = 500,
                    minHeight = 100.dp
                )

                // Section: Dettagli
                SectionLabel("Dettagli")

                TripFlowTextField(
                    value = form.duration,
                    onValueChange = viewModel::onDurationChange,
                    label = "Durata (minuti) *",
                    placeholder = "es. 120",
                    error = form.durationError,
                    enabled = !form.isLoading,
                    isPassword = false,
                    keyboardType = KeyboardType.Number,
                    leading = { FieldIcon(Icons.Default.AccessTime) }
                )

                // Section: Prezzo e posti
                SectionLabel("Prezzo e disponibilità")

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
                    text = if (form.isLoading) "Aggiunta in corso…" else "Aggiungi attività",
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
private fun AddActivityTopBar(onBack: () -> Unit) {
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
                text = "Aggiungi attività",
                style = MaterialTheme.typography.titleLarge,
                color = TripFlowColors.TextPrimary
            )
            Text(
                text = "Compila il form per aggiungere una nuova attività al viaggio",
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
                text = "Attività aggiunta!",
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
                Text("Torna al viaggio", color = TripFlowColors.Accent)
            }
        },
        containerColor = TripFlowColors.Background,
        tonalElevation = 0.dp
    )
}

