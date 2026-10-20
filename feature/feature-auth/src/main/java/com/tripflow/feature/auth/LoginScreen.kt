package com.tripflow.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import kotlinx.coroutines.launch

// --- Helpers di validazione ---
private fun isEmailValid(email: String): Boolean =
    email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

private fun isPasswordValidForLogin(password: String): Boolean =
    password.length >= 6

// --------------------------------

@Composable
fun LoginScreen(
    authRepository: AuthRepository = AuthRepository(),
    onLoginClick: (UserResponse) -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onContinueWithoutLogin: () -> Unit = {},
    successMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Validazione in tempo reale
    val emailError: String? = when {
        email.isEmpty() -> null
        !isEmailValid(email) -> "Inserisci un'email valida (es. nome@esempio.it)"
        else -> null
    }
    val passwordError: String? = when {
        password.isEmpty() -> null
        !isPasswordValidForLogin(password) -> "La password deve essere di almeno 6 caratteri"
        else -> null
    }

    val emailIsValid = email.isNotEmpty() && emailError == null
    val passwordIsValid = password.isNotEmpty() && passwordError == null

    LaunchedEffect(successMessage) {
        successMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = TripFlowColors.Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isSuccess = data.visuals.message == successMessage
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isSuccess) TripFlowColors.Accent else TripFlowColors.Error,
                    contentColor = TripFlowColors.Background
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = TripFlowColors.Accent,
                    modifier = Modifier
                        .background(TripFlowColors.AccentSoft, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                )
                Text("TripFlow", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(Dimens.gapL))
            Text("Bentornato", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Accedi per vedere le tue prenotazioni e i tuoi itinerari.",
                style = MaterialTheme.typography.bodyLarge,
                color = TripFlowColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(Dimens.gapS))
            TripFlowTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "nome@esempio.it",
                error = emailError,
                isValid = emailIsValid,
                keyboardType = KeyboardType.Email,
                leading = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TripFlowColors.TextSecondary) }
            )
            TripFlowTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "La tua password",
                isPassword = true,
                error = passwordError,
                isValid = passwordIsValid,
                leading = { Icon(Icons.Default.Lock, contentDescription = null, tint = TripFlowColors.TextSecondary) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onForgotPasswordClick,
                    enabled = !isLoading
                ) {
                    Text(
                        "Password dimenticata?",
                        color = TripFlowColors.Accent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            PrimaryButton(
                text = "Accedi",
                onClick = {
                    val emailOk = isEmailValid(email)
                    val passwordOk = isPasswordValidForLogin(password)
                    if (emailOk && passwordOk) {
                        isLoading = true
                        scope.launch {
                            try {
                                val result = authRepository.login(email, password)
                                result.onSuccess { user -> onLoginClick(user) }
                                result.onFailure {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Accesso non riuscito. Controlla email e password.")
                                    }
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (!emailOk) "Inserisci un'email valida"
                                else "La password deve essere di almeno 6 caratteri"
                            )
                        }
                    }
                },
                enabled = !isLoading
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = TripFlowColors.Divider)
                Text("oppure", modifier = Modifier.padding(horizontal = 12.dp), color = TripFlowColors.TextSecondary)
                HorizontalDivider(modifier = Modifier.weight(1f), color = TripFlowColors.Divider)
            }
            SecondaryButton(
                text = "Continua senza accedere",
                onClick = onContinueWithoutLogin,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Non hai un account? ", color = TripFlowColors.TextSecondary)
                TextButton(onClick = onRegisterClick, enabled = !isLoading) {
                    Text("Registrati", color = TripFlowColors.Accent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    TripFlowTheme { LoginScreen() }
}
