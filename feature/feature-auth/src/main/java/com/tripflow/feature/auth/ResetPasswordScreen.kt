package com.tripflow.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

// Schermata di reset password con codice OTP: dallo schermo precedente arriva l'email,
// l'utente inserisce il codice ricevuto via mail e la nuova password.
@Composable
fun ResetPasswordScreen(
    email: String,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onResetComplete: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(email) { viewModel.setEmail(email) }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onResetComplete()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
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
                .padding(horizontal = Dimens.screenPadding, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Torna indietro", tint = TripFlowColors.TextPrimary)
                }
                Text("Nuova password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TripFlowColors.TextPrimary)
            }

            Spacer(modifier = Modifier.height(Dimens.gapM))

            if (!state.isCompleted) {
                Box(modifier = Modifier.background(TripFlowColors.AccentSoft, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TripFlowColors.Accent)
                }
                Text("Imposta una nuova password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TripFlowColors.TextPrimary)
                Text(
                    "Inserisci il codice di 6 cifre che abbiamo inviato a ${state.email} e scegli una nuova password (almeno 8 caratteri).",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TripFlowColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(Dimens.gapS))

                TripFlowTextField(
                    value = state.code,
                    onValueChange = viewModel::onCodeChange,
                    label = "Codice di verifica",
                    placeholder = "6 cifre",
                    error = state.codeError,
                    isValid = state.code.length == 6 && state.codeError == null,
                    keyboardType = KeyboardType.Number,
                    leading = { Icon(Icons.Default.Lock, contentDescription = null, tint = TripFlowColors.TextSecondary) }
                )

                TripFlowTextField(
                    value = state.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    label = "Nuova password",
                    placeholder = "Almeno 8 caratteri",
                    error = state.newPasswordError,
                    isValid = state.newPassword.isNotEmpty() && state.newPasswordError == null,
                    isPassword = true,
                    leading = { Icon(Icons.Default.Lock, contentDescription = null, tint = TripFlowColors.TextSecondary) }
                )
                TripFlowTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = "Conferma password",
                    placeholder = "Ripeti la password",
                    error = state.confirmPasswordError,
                    isValid = state.confirmPassword.isNotEmpty() && state.confirmPasswordError == null,
                    isPassword = true,
                    leading = { Icon(Icons.Default.Lock, contentDescription = null, tint = TripFlowColors.TextSecondary) }
                )

                Spacer(modifier = Modifier.height(Dimens.gapS))

                PrimaryButton(
                    text = "Reimposta password",
                    onClick = viewModel::resetPassword,
                    enabled = !state.isLoading,
                    loading = state.isLoading
                )
            } else {
                Box(modifier = Modifier.background(TripFlowColors.AccentSoft, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TripFlowColors.Accent)
                }
                Text("Password aggiornata", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TripFlowColors.TextPrimary)
                Text(
                    "La tua password è stata reimpostata. Ora puoi accedere con le nuove credenziali.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TripFlowColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(Dimens.gapM))

                PrimaryButton(text = "Torna al Login", onClick = onResetComplete)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetPasswordScreenPreview() {
    TripFlowTheme { }
}
