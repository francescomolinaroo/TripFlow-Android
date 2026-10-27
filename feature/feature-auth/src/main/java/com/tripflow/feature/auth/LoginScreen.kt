package com.tripflow.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.LocalSessionKey
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(key = "login-${LocalSessionKey.current}"),
    onLoginClick: (UserResponse) -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: (email: String) -> Unit = {},
    onContinueWithoutLogin: () -> Unit = {},
    onNeedsVerification: (email: String) -> Unit = {},
    successMessage: String? = null
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.loggedInUser) {
        state.loggedInUser?.let { user ->
            viewModel.onNavigated()
            onLoginClick(user)
        }
    }

    LaunchedEffect(state.needsVerificationEmail) {
        state.needsVerificationEmail?.let { email ->
            viewModel.onNavigated()
            onNeedsVerification(email)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

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
        ) {
            AuthHeader(imageRes = R.drawable.bg_login_montagna)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.screenPadding)
                    .padding(top = Dimens.gapS, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
            ) {
            Text("Bentornato", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Accedi per vedere le tue prenotazioni e i tuoi itinerari.",
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
                leading = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TripFlowColors.TextSecondary) }
            )
            TripFlowTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                placeholder = "La tua password",
                isPassword = true,
                error = state.passwordError,
                isValid = state.password.isNotEmpty() && state.passwordError == null,
                leading = { Icon(Icons.Default.Lock, contentDescription = null, tint = TripFlowColors.TextSecondary) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onForgotPasswordClick(state.email.trim()) },
                    enabled = !state.isLoading
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
                onClick = viewModel::login,
                enabled = !state.isLoading,
                loading = state.isLoading
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = TripFlowColors.Divider)
                Text("oppure", modifier = Modifier.padding(horizontal = 12.dp), color = TripFlowColors.TextSecondary)
                HorizontalDivider(modifier = Modifier.weight(1f), color = TripFlowColors.Divider)
            }
            SecondaryButton(
                text = "Continua senza accedere",
                onClick = onContinueWithoutLogin,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(Dimens.gapL))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Non hai un account? ", color = TripFlowColors.TextSecondary)
                TextButton(onClick = onRegisterClick, enabled = !state.isLoading) {
                    Text("Registrati", color = TripFlowColors.Accent, fontWeight = FontWeight.Bold)
                }
            }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    TripFlowTheme { }
}
