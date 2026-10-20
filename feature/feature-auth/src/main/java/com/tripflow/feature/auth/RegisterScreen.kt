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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.RegisterRequest
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// --- Helpers di validazione ---
private fun isEmailValid(email: String): Boolean =
    email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

private fun isPasswordValid(password: String): Boolean =
    password.length >= 8

private fun isDateOfBirthValid(date: String): Boolean =
    date.isBlank() || Regex("""^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$""").matches(date)

private fun isPhoneValid(phone: String): Boolean =
    phone.isBlank() || Regex("""^\+?[\d\s\-]{6,20}$""").matches(phone)
// --------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authRepository: AuthRepository = AuthRepository(),
    onRegisterClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") } // Conserva sempre il formato ISO yyyy-MM-dd per il DB
    var selectedRole by remember { mutableStateOf("TRAVELER") }
    var showOptionalFields by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Stato del DatePicker con blocco delle date future (data di nascita nel passato)
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    // Formattazione a video in formato italiano gg/mm/aaaa
    val displayDateOfBirth = remember(dateOfBirth) {
        if (dateOfBirth.isNotBlank()) {
            try {
                val parsed = LocalDate.parse(dateOfBirth)
                DateTimeFormatter.ofPattern("dd/MM/yyyy").format(parsed)
            } catch (e: Exception) {
                dateOfBirth
            }
        } else ""
    }

    // ---- Validazione in tempo reale ----
    val firstNameError: String? = if (firstName.isNotEmpty() && firstName.isBlank()) "Il nome non può essere solo spazi" else null
    val firstNameIsValid = firstName.isNotBlank()

    val lastNameError: String? = if (lastName.isNotEmpty() && lastName.isBlank()) "Il cognome non può essere solo spazi" else null
    val lastNameIsValid = lastName.isNotBlank()

    val emailError: String? = when {
        email.isEmpty() -> null
        !isEmailValid(email) -> "Inserisci un'email valida (es. nome@esempio.it)"
        else -> null
    }
    val emailIsValid = email.isNotEmpty() && emailError == null

    val passwordError: String? = when {
        password.isEmpty() -> null
        !isPasswordValid(password) -> "La password deve essere di almeno 8 caratteri"
        else -> null
    }
    val passwordIsValid = password.isNotEmpty() && passwordError == null

    val confirmPasswordError: String? = when {
        confirmPassword.isEmpty() -> null
        confirmPassword != password -> "Le password non corrispondono"
        else -> null
    }
    val confirmPasswordIsValid = confirmPassword.isNotEmpty() && confirmPasswordError == null

    val dateOfBirthError: String? = when {
        dateOfBirth.isEmpty() -> null
        !isDateOfBirthValid(dateOfBirth) -> "Formato non valido. Usa aaaa-mm-gg"
        else -> null
    }
    val dateOfBirthIsValid = dateOfBirth.isNotEmpty() && dateOfBirthError == null

    val phoneError: String? = when {
        phoneNumber.isEmpty() -> null
        !isPhoneValid(phoneNumber) -> "Numero di telefono non valido"
        else -> null
    }
    val phoneIsValid = phoneNumber.isNotEmpty() && phoneError == null

    val formIsValid = firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            isEmailValid(email) &&
            isPasswordValid(password) &&
            confirmPassword == password &&
            (dateOfBirth.isEmpty() || isDateOfBirthValid(dateOfBirth)) &&
            (phoneNumber.isEmpty() || isPhoneValid(phoneNumber))
    // ------------------------------------

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            dateOfBirth = selectedDate.toString() // yyyy-MM-dd
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = TripFlowColors.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla", color = TripFlowColors.TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = TripFlowColors.Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = TripFlowColors.Error,
                    contentColor = TripFlowColors.Background
                )
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
            Text(
                text = "Crea il tuo account",
                style = MaterialTheme.typography.headlineMedium,
                color = TripFlowColors.TextPrimary
            )
            Text(
                text = "Inserisci i tuoi dati per iniziare a viaggiare con TripFlow.",
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(Dimens.gapS))

            AuthSection(title = "DATI PERSONALI") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
                ) {
                    RoleOption(
                        title = "Viaggio",
                        selected = selectedRole == "TRAVELER",
                        onClick = { selectedRole = "TRAVELER" },
                        modifier = Modifier.weight(1f)
                    )
                    RoleOption(
                        title = "Organizzo",
                        selected = selectedRole == "ORGANIZER",
                        onClick = { selectedRole = "ORGANIZER" },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
                    TripFlowTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "Nome",
                        placeholder = "Il tuo nome",
                        error = firstNameError,
                        isValid = firstNameIsValid,
                        modifier = Modifier.weight(1f)
                    )
                    TripFlowTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Cognome",
                        placeholder = "Il tuo cognome",
                        error = lastNameError,
                        isValid = lastNameIsValid,
                        modifier = Modifier.weight(1f)
                    )
                }
                TripFlowTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "nome@esempio.it",
                    error = emailError,
                    isValid = emailIsValid,
                    keyboardType = KeyboardType.Email
                )
            }

            AuthSection(title = "SICUREZZA") {
                TripFlowTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Almeno 8 caratteri",
                    error = passwordError,
                    isValid = passwordIsValid,
                    isPassword = true
                )
                TripFlowTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Conferma password",
                    placeholder = "Ripeti la password",
                    error = confirmPasswordError,
                    isValid = confirmPasswordIsValid,
                    isPassword = true
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOptionalFields = !showOptionalFields },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    TripFlowTextField(
                        value = displayDateOfBirth,
                        onValueChange = {},
                        label = "Data di nascita",
                        placeholder = "Seleziona data di nascita",
                        error = dateOfBirthError,
                        isValid = dateOfBirthIsValid,
                        trailing = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Seleziona data",
                                    tint = TripFlowColors.TextSecondary
                                )
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
                TripFlowTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Telefono",
                    placeholder = "+39 000 000 0000",
                    error = phoneError,
                    isValid = phoneIsValid,
                    keyboardType = KeyboardType.Phone
                )
            }

            Spacer(modifier = Modifier.height(Dimens.gapS))

            PrimaryButton(
                text = "Registrati",
                onClick = {
                    if (formIsValid) {
                        isLoading = true
                        scope.launch {
                            try {
                                val request = RegisterRequest(
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    password = password,
                                    role = selectedRole,
                                    dateOfBirth = dateOfBirth.ifBlank { null },
                                    phoneNumber = phoneNumber.ifBlank { null }
                                )
                                val result = authRepository.register(request)
                                result.onSuccess { onRegisterClick() }
                                result.onFailure { error ->
                                    val errorMsg = error.message.orEmpty()
                                    val userFriendlyMessage = when {
                                        errorMsg.contains("409") ->
                                            "Questa email è già registrata. Accedi con le tue credenziali."
                                        errorMsg.contains("429") ->
                                            "Troppe richieste in poco tempo. Riprova tra un minuto."
                                        else ->
                                            "Registrazione non riuscita. Controlla i dati inseriti."
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(userFriendlyMessage)
                                    }
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Correggi i campi evidenziati in rosso prima di procedere.")
                        }
                    }
                },
                enabled = !isLoading
            )
            SecondaryButton(
                text = "Hai già un account? Accedi",
                onClick = onLoginClick,
                enabled = !isLoading
            )
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
    val borderColor = if (selected) TripFlowColors.Accent else TripFlowColors.Border
    val backgroundColor = if (selected) TripFlowColors.AccentSoft else TripFlowColors.Background

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .height(52.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(Dimens.radiusButton))
            .background(backgroundColor, RoundedCornerShape(Dimens.radiusButton))
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
private fun AuthSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TripFlowColors.TextSecondary
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    TripFlowTheme {
        RegisterScreen()
    }
}