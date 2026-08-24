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
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository = AuthRepository(),
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onContinueWithoutLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                leading = { Icon(Icons.Default.Lock, contentDescription = null, tint = TripFlowColors.TextSecondary) }
            )

            PrimaryButton(
                text = "Accedi",
                onClick = {
                    emailError = if (email.isBlank()) "Inserisci l'email" else null
                    passwordError = if (password.isBlank()) "Inserisci la password" else null
                    if (emailError == null && passwordError == null) {
                        isLoading = true
                        scope.launch {
                            try {
                                val result = authRepository.login(email, password)
                                result.onSuccess { onLoginClick() }
                                result.onFailure {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Accesso non riuscito. Controlla email e password.")
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
