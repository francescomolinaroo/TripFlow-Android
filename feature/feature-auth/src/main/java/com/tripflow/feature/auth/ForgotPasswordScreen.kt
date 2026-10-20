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

private fun isEmailValid(email: String): Boolean =
    email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

@Composable
fun ForgotPasswordScreen(
    authRepository: AuthRepository = AuthRepository(),
    onBackClick: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val emailError: String? = when {
        email.isEmpty() -> null
        !isEmailValid(email) -> "Inserisci un'email valida (es. nome@esempio.it)"
        else -> null
    }
    val emailIsValid = email.isNotEmpty() && emailError == null

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
                .padding(horizontal = Dimens.screenPadding, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.gapS)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Torna indietro",
                        tint = TripFlowColors.TextPrimary
                    )
                }
                Text(
                    text = "Recupero password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TripFlowColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(Dimens.gapM))

            if (!isSubmitted) {
                Box(
                    modifier = Modifier
                        .background(TripFlowColors.AccentSoft, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = TripFlowColors.Accent
                    )
                }

                Text(
                    "Hai dimenticato la password?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TripFlowColors.TextPrimary
                )
                Text(
                    "Inserisci l'indirizzo email associato al tuo account. Ti invieremo un link per creare una nuova password.",
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
                    leading = { Icon(Icons.Default.Email, contentDescription = null, tint = TripFlowColors.TextSecondary) }
                )

                Spacer(modifier = Modifier.height(Dimens.gapS))

                PrimaryButton(
                    text = "Invia link di recupero",
                    onClick = {
                        if (isEmailValid(email)) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val result = authRepository.forgotPassword(email.trim())
                                    result.onSuccess {
                                        isSubmitted = true
                                    }
                                    result.onFailure {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Impossibile inviare la richiesta. Riprova più tardi.")
                                        }
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Inserisci un indirizzo email valido")
                            }
                        }
                    },
                    enabled = !isLoading
                )
            } else {
                Box(
                    modifier = Modifier
                        .background(TripFlowColors.AccentSoft, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = TripFlowColors.Accent
                    )
                }

                Text(
                    "Controlla la tua email",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TripFlowColors.TextPrimary
                )
                Text(
                    "Se l'indirizzo $email è associato a un account TripFlow, abbiamo inviato una mail con le istruzioni e il link per reimpostare la tua password.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TripFlowColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(Dimens.gapM))

                PrimaryButton(
                    text = "Torna al Login",
                    onClick = onSuccess
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    TripFlowTheme { ForgotPasswordScreen() }
}
