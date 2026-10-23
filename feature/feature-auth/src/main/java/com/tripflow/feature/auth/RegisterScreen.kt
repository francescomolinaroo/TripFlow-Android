package com.tripflow.feature.auth

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterClick: (email: String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showOptionalFields by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            val email = state.email
            viewModel.onNavigated()
            onRegisterClick(email)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
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

    Scaffold(
        containerColor = TripFlowColors.Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = TripFlowColors.Error, contentColor = TripFlowColors.Background)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
            }
            Text("Crea il tuo account", style = MaterialTheme.typography.headlineMedium, color = TripFlowColors.TextPrimary)
            Text("Inserisci i tuoi dati per iniziare a viaggiare con TripFlow.", style = MaterialTheme.typography.bodyMedium, color = TripFlowColors.TextSecondary)

            Spacer(modifier = Modifier.height(Dimens.gapS))

            AuthSection(title = "DATI PERSONALI") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
                    RoleOption("Viaggio", state.selectedRole == "TRAVELER", { viewModel.onRoleChange("TRAVELER") }, Modifier.weight(1f))
                    RoleOption("Organizzo", state.selectedRole == "ORGANIZER", { viewModel.onRoleChange("ORGANIZER") }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
                    TripFlowTextField(
                        value = state.firstName,
                        onValueChange = viewModel::onFirstNameChange,
                        label = "Nome",
                        placeholder = "Il tuo nome",
                        error = state.firstNameError,
                        isValid = state.firstName.isNotBlank() && state.firstNameError == null,
                        modifier = Modifier.weight(1f)
                    )
                    TripFlowTextField(
                        value = state.lastName,
                        onValueChange = viewModel::onLastNameChange,
                        label = "Cognome",
                        placeholder = "Il tuo cognome",
                        error = state.lastNameError,
                        isValid = state.lastName.isNotBlank() && state.lastNameError == null,
                        modifier = Modifier.weight(1f)
                    )
                }
                TripFlowTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Email",
                    placeholder = "nome@esempio.it",
                    error = state.emailError,
                    isValid = state.email.isNotEmpty() && state.emailError == null,
                    keyboardType = KeyboardType.Email
                )
            }

            AuthSection(title = "SICUREZZA") {
                TripFlowTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    placeholder = "Almeno 8 caratteri",
                    error = state.passwordError,
                    isValid = state.password.isNotEmpty() && state.passwordError == null,
                    isPassword = true
                )
                TripFlowTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = "Conferma password",
                    placeholder = "Ripeti la password",
                    error = state.confirmPasswordError,
                    isValid = state.confirmPassword.isNotEmpty() && state.confirmPasswordError == null,
                    isPassword = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().clickable { showOptionalFields = !showOptionalFields },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dati facoltativi", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (showOptionalFields) "Nascondi" else "Data di nascita, telefono",
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary
                )
            }

            if (showOptionalFields) {
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    TripFlowTextField(
                        value = displayDateOfBirth,
                        onValueChange = {},
                        label = "Data di nascita",
                        placeholder = "Seleziona data di nascita",
                        error = state.dateOfBirthError,
                        isValid = state.dateOfBirth.isNotEmpty() && state.dateOfBirthError == null,
                        trailing = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Seleziona data", tint = TripFlowColors.TextSecondary)
                            }
                        }
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }
                TripFlowTextField(
                    value = state.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    label = "Telefono",
                    placeholder = "+39 000 000 0000",
                    error = state.phoneError,
                    isValid = state.phoneNumber.isNotEmpty() && state.phoneError == null,
                    keyboardType = KeyboardType.Phone
                )
            }

            Spacer(modifier = Modifier.height(Dimens.gapS))

            PrimaryButton(text = "Registrati", onClick = viewModel::register, enabled = !state.isLoading, loading = state.isLoading)
            SecondaryButton(text = "Hai già un account? Accedi", onClick = onLoginClick, enabled = !state.isLoading)
            Spacer(modifier = Modifier.height(Dimens.gapXL))
        }
    }
}

@Composable
private fun RoleOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .border(1.5.dp, if (selected) TripFlowColors.Accent else TripFlowColors.Border, RoundedCornerShape(Dimens.radiusButton))
            .background(if (selected) TripFlowColors.AccentSoft else TripFlowColors.Background, RoundedCornerShape(Dimens.radiusButton))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) TripFlowColors.AccentDark else TripFlowColors.TextBody
        )
    }
}

@Composable
private fun AuthSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    TripFlowTheme { }
}
