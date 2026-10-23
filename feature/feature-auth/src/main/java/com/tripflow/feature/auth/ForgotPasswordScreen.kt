package com.tripflow.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
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

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSuccess: (email: String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                Text("Recupero password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TripFlowColors.TextPrimary)
            }

            Spacer(modifier = Modifier.height(Dimens.gapM))

            if (!state.isSubmitted) {
                Box(modifier = Modifier.background(TripFlowColors.AccentSoft, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = TripFlowColors.Accent)
                }
                Text("Hai dimenticato la password?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TripFlowColors.TextPrimary)
                Text(
                    "Inserisci l'indirizzo email associato al tuo account. Ti invieremo un codice per creare una nuova password.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TripFlowColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(Dimens.gapS))

                TripFlowTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Email",
                    placeholder = "nome@esempio.it",
                    error = state.emailError,
                    isValid = state.email.isNotEmpty() && state.emailError == null,
                    keyboardType = KeyboardType.Email,
                    leading = { Icon(Icons.Default.Email, contentDescription = null, tint = TripFlowColors.TextSecondary) }
                )

                Spacer(modifier = Modifier.height(Dimens.gapS))

                PrimaryButton(text = "Invia codice di recupero", onClick = viewModel::submit, enabled = !state.isLoading, loading = state.isLoading)
            } else {
                Box(modifier = Modifier.background(TripFlowColors.AccentSoft, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TripFlowColors.Accent)
                }
                Text("Controlla la tua email", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TripFlowColors.TextPrimary)
                Text(
                    "Se l'indirizzo ${state.email} è associato a un account TripFlow, abbiamo inviato una mail con un codice per reimpostare la tua password.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TripFlowColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(Dimens.gapM))

                PrimaryButton(text = "Inserisci il codice", onClick = { onSuccess(state.email) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    TripFlowTheme { }
}
