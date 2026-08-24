package com.tripflow.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(containerColor = TripFlowColors.Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            Text(
                text = "Accedi a TripFlow",
                style = MaterialTheme.typography.headlineMedium,
                color = TripFlowColors.TextPrimary
            )
            Text(
                text = "Accedi per gestire i tuoi viaggi e le prenotazioni.",
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(Dimens.gapS))

            TripFlowTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "nome@esempio.it",
                keyboardType = KeyboardType.Email
            )
            TripFlowTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "La tua password",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(Dimens.gapS))

            PrimaryButton(
                text = "Accedi",
                onClick = onLoginClick
            )
            SecondaryButton(
                text = "Non hai un account? Registrati",
                onClick = onRegisterClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    TripFlowTheme {
        LoginScreen()
    }
}
