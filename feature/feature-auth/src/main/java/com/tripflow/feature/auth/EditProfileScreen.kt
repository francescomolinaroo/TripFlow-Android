package com.tripflow.feature.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.component.DangerButton
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.LocalSessionKey
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: UserResponse,
    viewModel: EditProfileViewModel = hiltViewModel(key = "edit-profile-${LocalSessionKey.current}"),
    onBackClick: () -> Unit = {},
    onSaved: (UserResponse) -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    onPasswordChanged: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= System.currentTimeMillis()
        }
    )

    val displayDateOfBirth = remember(state.dateOfBirth) {
        if (state.dateOfBirth.isNotBlank()) {
            try {
                DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.parse(state.dateOfBirth))
            } catch (_: Exception) { state.dateOfBirth }
        } else ""
    }

    LaunchedEffect(user) { viewModel.init(user) }

    LaunchedEffect(state.savedUser) {
        state.savedUser?.let { saved ->
            delay(900) // mostra il messaggio di conferma prima di tornare alla dashboard
            viewModel.onNavigated()
            onSaved(saved)
        }
    }

    // blocca il "indietro" di sistema mentre si mostra l'avviso: la sessione è già chiusa
    BackHandler(enabled = state.passwordChanged) {}

    LaunchedEffect(state.passwordChanged) {
        if (state.passwordChanged) {
            delay(2500) // tempo per leggere l'avviso prima di tornare al login
            viewModel.onNavigated()
            onPasswordChanged()
        }
    }

    LaunchedEffect(state.isAccountDeleted) {
        if (state.isAccountDeleted) {
            viewModel.onNavigated()
            onAccountDeleted()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.uploadAvatar(uri, context)
    }

    Scaffold(containerColor = TripFlowColors.Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, enabled = !state.passwordChanged) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
                Text("Modifica profilo", style = MaterialTheme.typography.headlineSmall)
            }

            UserBadge(
                name = if (state.firstName.isBlank() && state.lastName.isBlank()) null else "${state.firstName} ${state.lastName}",
                imageUrl = state.profileImage.ifBlank { null },
                size = 80.dp
            )
            SecondaryButton(
                text = if (state.isUploadingAvatar) "Caricamento in corso..." else "Scegli immagine profilo",
                onClick = { imagePicker.launch(arrayOf("image/*")) },
                enabled = !state.isLoading && !state.isUploadingAvatar
            )

            EditSection(title = "DATI PERSONALI") {
                TripFlowTextField(
                    value = state.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    label = "Nome",
                    error = state.firstNameError,
                    isValid = state.firstName.isNotBlank() && state.firstNameError == null
                )
                TripFlowTextField(
                    value = state.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    label = "Cognome",
                    error = state.lastNameError,
                    isValid = state.lastName.isNotBlank() && state.lastNameError == null
                )
                TripFlowTextField(
                    value = state.email,
                    onValueChange = {},
                    label = "Email",
                    enabled = false
                )
                if (state.dateOfBirthLocked) {
                    TripFlowTextField(
                        value = displayDateOfBirth,
                        onValueChange = {},
                        label = "Data di nascita (non modificabile)",
                        enabled = false
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                        TripFlowTextField(
                            value = displayDateOfBirth,
                            onValueChange = {},
                            label = "Data di nascita",
                            placeholder = "Una volta salvata non potrai modificarla",
                            error = state.dateOfBirthError,
                            isValid = state.dateOfBirth.isNotBlank() && state.dateOfBirthError == null,
                            trailing = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Seleziona data", tint = TripFlowColors.TextSecondary)
                                }
                            }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                    }
                }
                TripFlowTextField(
                    value = state.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    label = "Telefono",
                    placeholder = "+39 000 000 0000",
                    error = state.phoneError,
                    isValid = state.phoneNumber.isNotBlank() && state.phoneError == null,
                    keyboardType = KeyboardType.Phone
                )
            }

            EditSection(title = "CAMBIA PASSWORD") {
                TripFlowTextField(
                    value = state.currentPassword,
                    onValueChange = viewModel::onCurrentPasswordChange,
                    label = "Password attuale",
                    placeholder = "La password che usi ora",
                    error = state.currentPasswordError,
                    isValid = state.currentPassword.isNotBlank() && state.currentPasswordError == null,
                    isPassword = true
                )
                TripFlowTextField(
                    value = state.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    label = "Nuova password",
                    placeholder = "Almeno 8 caratteri",
                    error = state.newPasswordError,
                    isValid = state.newPassword.isNotBlank() && state.newPasswordError == null,
                    isPassword = true
                )
                TripFlowTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = "Conferma nuova password",
                    placeholder = "Ripeti la password",
                    error = state.confirmPasswordError,
                    isValid = state.confirmPassword.isNotBlank() && state.confirmPasswordError == null,
                    isPassword = true
                )
            }

            if (state.errorMessage != null) {
                Text(state.errorMessage.orEmpty(), color = TripFlowColors.Error)
            }
            if (state.successMessage != null) {
                Text(state.successMessage.orEmpty(), color = TripFlowColors.Success)
            }

            // dopo il cambio password la sessione è chiusa: niente altre azioni fino al login
            val actionsEnabled = !state.isLoading && !state.passwordChanged
            PrimaryButton(text = "Salva modifiche", onClick = viewModel::save, enabled = actionsEnabled && !state.isUploadingAvatar, loading = state.isLoading)
            SecondaryButton(text = "Annulla", onClick = onBackClick, enabled = actionsEnabled)
            DangerButton(text = "Elimina account", onClick = { viewModel.onShowDeleteDialogChange(true) }, enabled = actionsEnabled)
            Spacer(modifier = Modifier.height(Dimens.gapS))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.onDateOfBirthChange(selected.toString())
                    }
                    showDatePicker = false
                }) { Text("OK", color = TripFlowColors.Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla", color = TripFlowColors.TextSecondary)
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onShowDeleteDialogChange(false) },
            title = { Text("Eliminare l'account?") },
            text = { Text("Questa operazione non può essere annullata.") },
            confirmButton = {
                TextButton(onClick = viewModel::deleteAccount) {
                    Text("Elimina", color = TripFlowColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDeleteDialogChange(false) }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun EditSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    TripFlowTheme { }
}
