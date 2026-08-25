package com.tripflow.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.RegisterRequest
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import kotlinx.coroutines.launch

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
    var dateOfBirth by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("TRAVELER") }
    var showOptionalFields by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val firstNameError = if (submitted && firstName.isBlank()) "Inserisci il nome" else null
    val lastNameError = if (submitted && lastName.isBlank()) "Inserisci il cognome" else null
    val emailError = when {
        !submitted -> null
        email.isBlank() -> "Inserisci l'email"
        !email.contains("@") -> "Inserisci un'email valida"
        else -> null
    }
    val passwordError = when {
        !submitted -> null
        password.isBlank() -> "Inserisci la password"
        password.length < 8 -> "La password deve avere almeno 8 caratteri"
        else -> null
    }
    val confirmPasswordError = when {
        !submitted -> null
        confirmPassword.isBlank() -> "Conferma la password"
        confirmPassword != password -> "Le password non coincidono"
        else -> null
    }
    val formIsValid = listOf(
        firstName.isBlank(),
        lastName.isBlank(),
        email.isBlank(),
        !email.contains("@"),
        password.isBlank(),
        password.length < 8,
        confirmPassword.isBlank(),
        confirmPassword != password
    ).none { it }

    Scaffold(
        containerColor = TripFlowColors.Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
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
                        modifier = Modifier.weight(1f)
                    )
                    TripFlowTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Cognome",
                        placeholder = "Il tuo cognome",
                        error = lastNameError,
                        modifier = Modifier.weight(1f)
                    )
                }
                TripFlowTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "nome@esempio.it",
                    error = emailError,
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
                    isPassword = true
                )
                TripFlowTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Conferma password",
                    placeholder = "Ripeti la password",
                    error = confirmPasswordError,
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
                TripFlowTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = "Data di nascita",
                    placeholder = "gg/mm/aaaa"
                )
                TripFlowTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Telefono",
                    placeholder = "+39 000 000 0000",
                    keyboardType = KeyboardType.Phone
                )
            }

            Spacer(modifier = Modifier.height(Dimens.gapS))

            PrimaryButton(
                text = "Registrati",
                onClick = {
                    submitted = true
                    if (formIsValid) {
                        isLoading = true
                        scope.launch {
                            try {
                                val request = RegisterRequest(
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    password = password,
                                    role = selectedRole
                                )
                                val result = authRepository.register(request)
                                result.onSuccess { onRegisterClick() }
                                result.onFailure {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Registrazione non riuscita. Controlla i dati inseriti.")
                                    }
                                }
                            } finally {
                                isLoading = false
                            }
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
