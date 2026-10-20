package com.tripflow.feature.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.UpdateProfileRequest
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.component.DangerButton
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import kotlinx.coroutines.launch

// --- Helpers di validazione ---
private fun isDateValid(date: String): Boolean =
    date.isBlank() || Regex("""^\d{4}-\d{2}-\d{2}$""").matches(date)

private fun isPhoneValid(phone: String): Boolean =
    phone.isBlank() || phone.length in 7..20

private fun isPasswordValid(password: String): Boolean =
    password.isBlank() || password.length >= 8

// --------------------------------

@Composable
fun EditProfileScreen(
    user: UserResponse,
    authRepository: AuthRepository = AuthRepository(),
    onBackClick: () -> Unit = {},
    onSaved: (UserResponse) -> Unit = {},
    onAccountDeleted: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var dateOfBirth by remember { mutableStateOf(user.dateOfBirth.orEmpty()) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber.orEmpty()) }
    var profileImage by remember { mutableStateOf(user.profileImage.orEmpty()) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isUploadingAvatar = true
            errorMessage = null
            scope.launch {
                val uploadResult = authRepository.uploadAvatar(uri, context)
                isUploadingAvatar = false
                uploadResult.onSuccess { url ->
                    profileImage = url
                    successMessage = "Immagine del profilo caricata!"
                }
                uploadResult.onFailure { error ->
                    errorMessage = error.message ?: "Errore durante il caricamento dell'immagine"
                }
            }
        }
    }

    // Validazione in tempo reale
    val firstNameError: String? = when {
        firstName.isBlank() -> "Il nome non puo essere vuoto"
        firstName.length > 100 -> "Massimo 100 caratteri"
        else -> null
    }
    val lastNameError: String? = when {
        lastName.isBlank() -> "Il cognome non puo essere vuoto"
        lastName.length > 100 -> "Massimo 100 caratteri"
        else -> null
    }
    val dateOfBirthError: String? = when {
        !isDateValid(dateOfBirth) -> "Formato non valido: usa AAAA-MM-GG"
        else -> null
    }
    val phoneError: String? = when {
        !isPhoneValid(phoneNumber) -> "Numero di telefono non valido (7-20 cifre)"
        else -> null
    }
    val newPasswordError: String? = when {
        newPassword.isNotBlank() && !isPasswordValid(newPassword) -> "La password deve essere di almeno 8 caratteri"
        else -> null
    }
    val confirmPasswordError: String? = when {
        newPassword.isNotBlank() && confirmPassword != newPassword -> "Le password non coincidono"
        else -> null
    }

    val firstNameIsValid = firstName.isNotBlank() && firstNameError == null
    val lastNameIsValid = lastName.isNotBlank() && lastNameError == null
    val dateOfBirthIsValid = dateOfBirth.isNotBlank() && dateOfBirthError == null
    val phoneIsValid = phoneNumber.isNotBlank() && phoneError == null
    val newPasswordIsValid = newPassword.isNotBlank() && newPasswordError == null
    val confirmPasswordIsValid = confirmPassword.isNotBlank() && confirmPasswordError == null

    val formIsValid = firstNameError == null &&
        lastNameError == null &&
        dateOfBirthError == null &&
        phoneError == null &&
        newPasswordError == null &&
        confirmPasswordError == null

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
                Text("Modifica profilo", style = MaterialTheme.typography.headlineSmall)
            }

            UserBadge(
                name = if (firstName.isBlank() && lastName.isBlank()) null else "$firstName $lastName",
                imageUrl = profileImage.ifBlank { null },
                size = 80.dp
            )
            SecondaryButton(
                text = if (isUploadingAvatar) "Caricamento in corso..." else "Scegli immagine profilo",
                onClick = { imagePicker.launch(arrayOf("image/*")) },
                enabled = !isLoading && !isUploadingAvatar
            )

            EditSection(title = "DATI PERSONALI") {
                TripFlowTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Nome",
                    error = firstNameError,
                    isValid = firstNameIsValid
                )
                TripFlowTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Cognome",
                    error = lastNameError,
                    isValid = lastNameIsValid
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
                    placeholder = "aaaa-mm-gg",
                    error = dateOfBirthError,
                    isValid = dateOfBirthIsValid
                )
                TripFlowTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Telefono",
                    placeholder = "+39 000 000 0000",
                    error = phoneError,
                    isValid = phoneIsValid,
                    keyboardType = KeyboardType.Phone
                )
            }

            EditSection(title = "CAMBIA PASSWORD") {
                TripFlowTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Nuova password",
                    placeholder = "Almeno 8 caratteri",
                    error = newPasswordError,
                    isValid = newPasswordIsValid,
                    isPassword = true
                )
                TripFlowTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Conferma nuova password",
                    placeholder = "Ripeti la password",
                    error = confirmPasswordError,
                    isValid = confirmPasswordIsValid,
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
                    if (!formIsValid) {
                        errorMessage = "Correggi i campi evidenziati prima di salvare"
                        return@PrimaryButton
                    }
                    errorMessage = null
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
                                onSaved(profileResult.getOrThrow())
                            } else {
                                errorMessage = passwordResult.exceptionOrNull()?.message ?: "Password non aggiornata"
                            }
                        } else {
                            isLoading = false
                            successMessage = "Profilo aggiornato"
                            onSaved(profileResult.getOrThrow())
                        }
                    }
                },
                enabled = !isLoading && !isUploadingAvatar
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
            text = { Text("Questa operazione non puo essere annullata.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isLoading = true
                        scope.launch {
                            val result = authRepository.deleteAccount()
                            isLoading = false
                            result.onSuccess {
                                onAccountDeleted()
                            }
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
