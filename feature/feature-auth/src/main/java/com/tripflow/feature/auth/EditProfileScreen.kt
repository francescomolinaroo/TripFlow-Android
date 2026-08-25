package com.tripflow.feature.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.UpdateProfileRequest
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.DangerButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    user: UserResponse,
    authRepository: AuthRepository = AuthRepository(),
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var dateOfBirth by remember { mutableStateOf(user.dateOfBirth.orEmpty()) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber.orEmpty()) }
    var profileImage by remember { mutableStateOf(user.profileImage.orEmpty()) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (exception: SecurityException) {
                profileImage = uri.toString()
            }
            profileImage = uri.toString()
        }
    }

    Scaffold(containerColor = TripFlowColors.Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                }
                Text("Modifica profilo", style = MaterialTheme.typography.headlineSmall)
            }

            UserBadge(
                name = if (firstName.isBlank() && lastName.isBlank()) null else "$firstName $lastName",
                imageUrl = profileImage.ifBlank { null },
                size = 80.dp
            )
            SecondaryButton(
                text = "Scegli immagine profilo",
                onClick = { imagePicker.launch(arrayOf("image/*")) },
                enabled = !isLoading
            )

            EditSection(title = "DATI PERSONALI") {
                TripFlowTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Nome"
                )
                TripFlowTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Cognome"
                )
                TripFlowTextField(
                    value = user.email,
                    onValueChange = {},
                    label = "Email",
                    enabled = false
                )
                TripFlowTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = "Data di nascita",
                    placeholder = "aaaa-mm-gg"
                )
                TripFlowTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Telefono",
                    placeholder = "+39 000 000 0000",
                    keyboardType = KeyboardType.Phone
                )
            }

            EditSection(title = "CAMBIA PASSWORD") {
                TripFlowTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Nuova password",
                    placeholder = "Almeno 8 caratteri",
                    isPassword = true
                )
                TripFlowTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Conferma nuova password",
                    placeholder = "Ripeti la password",
                    isPassword = true
                )
            }

            if (errorMessage != null) {
                Text(errorMessage.orEmpty(), color = TripFlowColors.Error)
            }
            if (successMessage != null) {
                Text(successMessage.orEmpty(), color = TripFlowColors.Success)
            }

            PrimaryButton(
                text = "Salva modifiche",
                onClick = {
                    val profileValid = firstName.isNotBlank() && lastName.isNotBlank()
                    val passwordValid = newPassword.isBlank() ||
                        (newPassword.length >= 8 && newPassword == confirmPassword)
                    errorMessage = when {
                        !profileValid -> "Nome e cognome sono obbligatori"
                        !passwordValid -> "Controlla la nuova password"
                        else -> null
                    }
                    if (errorMessage == null) {
                        isLoading = true
                        scope.launch {
                            val profileResult = authRepository.updateProfile(
                                UpdateProfileRequest(
                                    firstName = firstName,
                                    lastName = lastName,
                                    dateOfBirth = dateOfBirth.ifBlank { null },
                                    phoneNumber = phoneNumber.ifBlank { null },
                                    profileImage = profileImage.ifBlank { null }
                                )
                            )
                            if (profileResult.isFailure) {
                                errorMessage = profileResult.exceptionOrNull()?.message ?: "Profilo non aggiornato"
                                isLoading = false
                            } else if (newPassword.isNotBlank()) {
                                val passwordResult = authRepository.changePassword(newPassword)
                                isLoading = false
                                if (passwordResult.isSuccess) {
                                    successMessage = "Profilo e password aggiornati"
                                    onSaved()
                                } else {
                                    errorMessage = passwordResult.exceptionOrNull()?.message ?: "Password non aggiornata"
                                }
                            } else {
                                isLoading = false
                                successMessage = "Profilo aggiornato"
                                onSaved()
                            }
                        }
                    }
                },
                enabled = !isLoading
            )
            SecondaryButton(
                text = "Annulla",
                onClick = onBackClick,
                enabled = !isLoading
            )
            DangerButton(
                text = "Elimina account",
                onClick = { showDeleteDialog = true },
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(Dimens.gapS))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminare l'account?") },
            text = { Text("Questa operazione non può essere annullata.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isLoading = true
                        scope.launch {
                            val result = authRepository.deleteAccount()
                            isLoading = false
                            result.onSuccess { onBackClick() }
                            result.onFailure { exception ->
                                errorMessage = exception.message ?: "Account non eliminato"
                            }
                        }
                    }
                ) {
                    Text("Elimina", color = TripFlowColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun EditSection(
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
private fun EditProfileScreenPreview() {
    TripFlowTheme {
        EditProfileScreen(
            user = UserResponse(
                id = "1",
                firstName = "Mario",
                lastName = "Rossi",
                email = "mario@example.com",
                role = "TRAVELER",
                dateOfBirth = null,
                phoneNumber = null,
                profileImage = null
            )
        )
    }
}
