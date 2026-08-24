package com.tripflow.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@Composable
fun RegisterScreen(
    onRegisterClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

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
        firstNameError,
        lastNameError,
        emailError,
        passwordError,
        confirmPasswordError
    ).all { it == null }

    Scaffold(containerColor = TripFlowColors.Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
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
                TripFlowTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Nome",
                    placeholder = "Il tuo nome",
                    error = firstNameError
                )
                TripFlowTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Cognome",
                    placeholder = "Il tuo cognome",
                    error = lastNameError
                )
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

            Spacer(modifier = Modifier.height(Dimens.gapS))

            PrimaryButton(
                text = "Registrati",
                onClick = {
                    submitted = true
                    if (formIsValid) {
                        onRegisterClick()
                    }
                }
            )
            SecondaryButton(
                text = "Hai già un account? Accedi",
                onClick = onLoginClick
            )
        }
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
