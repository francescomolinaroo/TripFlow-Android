package com.tripflow.feature.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.auth.userMessage
import com.tripflow.core.network.auth.UpdateProfileRequest
import com.tripflow.core.network.auth.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val phonePrefix: String = PREFISSO_PREDEFINITO,
    val phoneNumber: String = "",
    val profileImage: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val dateOfBirthError: String? = null,
    val phoneError: String? = null,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val savedUser: UserResponse? = null,
    val isAccountDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    // password cambiata: la sessione è già chiusa, la schermata mostra l'avviso e torna al login
    val passwordChanged: Boolean = false,
    // la data di nascita si imposta una volta sola (il backend rifiuta le modifiche)
    val dateOfBirthLocked: Boolean = false,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    // Chiamata a ogni apertura della schermata: riparte dai dati salvati, scartando modifiche
    // non confermate e password digitate in una visita precedente.
    fun init(user: UserResponse) {
        // il numero salvato (+393331234567, o in un formato vecchio) torna diviso in prefisso e numero
        val (phonePrefix, phoneNumber) = dividiTelefono(user.phoneNumber)
        _uiState.value = EditProfileUiState(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            dateOfBirth = user.dateOfBirth.orEmpty(),
            phonePrefix = phonePrefix,
            phoneNumber = phoneNumber,
            phoneError = if (isNumeroValido(phoneNumber)) null else ERRORE_NUMERO,
            profileImage = user.profileImage.orEmpty(),
            dateOfBirthLocked = !user.dateOfBirth.isNullOrBlank(),
        )
    }

    fun onFirstNameChange(value: String) {
        val error = when {
            value.isBlank() -> "Il nome non può essere vuoto"
            value.length > 100 -> "Massimo 100 caratteri"
            else -> null
        }
        _uiState.update { it.copy(firstName = value, firstNameError = error, errorMessage = null) }
    }

    fun onLastNameChange(value: String) {
        val error = when {
            value.isBlank() -> "Il cognome non può essere vuoto"
            value.length > 100 -> "Massimo 100 caratteri"
            else -> null
        }
        _uiState.update { it.copy(lastName = value, lastNameError = error, errorMessage = null) }
    }

    fun onDateOfBirthChange(value: String) {
        if (_uiState.value.dateOfBirthLocked) return
        val error = if (!isDateOfBirthValid(value)) "Formato non valido: usa AAAA-MM-GG" else null
        _uiState.update { it.copy(dateOfBirth = value, dateOfBirthError = error) }
    }

    fun onPhonePrefixChange(prefix: String) {
        _uiState.update { it.copy(phonePrefix = prefix) }
    }

    fun onPhoneNumberChange(value: String) {
        val number = soloCifre(value)
        val error = if (isNumeroValido(number)) null else ERRORE_NUMERO
        _uiState.update { it.copy(phoneNumber = number, phoneError = error) }
    }

    fun onCurrentPasswordChange(value: String) {
        _uiState.update { it.copy(currentPassword = value, currentPasswordError = null, errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        val error = if (value.isNotBlank() && !isPasswordValidForRegister(value))
            "La password deve essere di almeno 8 caratteri" else null
        val confirmError = if (_uiState.value.confirmPassword.isNotBlank() && _uiState.value.confirmPassword != value)
            "Le password non coincidono" else null
        _uiState.update { it.copy(newPassword = value, newPasswordError = error, confirmPasswordError = confirmError) }
    }

    fun onConfirmPasswordChange(value: String) {
        val error = if (_uiState.value.newPassword.isNotBlank() && value != _uiState.value.newPassword)
            "Le password non coincidono" else null
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = error) }
    }

    fun onShowDeleteDialogChange(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun uploadAvatar(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, errorMessage = null) }
            val result = repository.uploadAvatar(uri, context)
            result.fold(
                onSuccess = { url -> _uiState.update { it.copy(isUploadingAvatar = false, profileImage = url, successMessage = "Immagine del profilo caricata!") } },
                onFailure = { error -> _uiState.update { it.copy(isUploadingAvatar = false, errorMessage = error.userMessage("Errore durante il caricamento dell'immagine")) } }
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isLoading) return

        val hasErrors = state.firstNameError != null || state.lastNameError != null ||
                state.dateOfBirthError != null || state.phoneError != null ||
                state.currentPasswordError != null ||
                state.newPasswordError != null || state.confirmPasswordError != null

        if (hasErrors) {
            _uiState.update { it.copy(errorMessage = "Correggi i campi evidenziati prima di salvare") }
            return
        }

        // per cambiare password serve la password attuale
        if (state.newPassword.isNotBlank() && state.currentPassword.isBlank()) {
            _uiState.update { it.copy(currentPasswordError = "Inserisci la password attuale per cambiarla") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val profileResult = repository.updateProfile(
                UpdateProfileRequest(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    dateOfBirth = state.dateOfBirth.ifBlank { null },
                    phoneNumber = componiTelefono(state.phonePrefix, state.phoneNumber)
                )
            )
            if (profileResult.isFailure) {
                val error = profileResult.exceptionOrNull()
                _uiState.update { it.copy(isLoading = false, errorMessage = error?.userMessage("Profilo non aggiornato") ?: "Profilo non aggiornato") }
                return@launch
            }
            if (state.newPassword.isNotBlank()) {
                val passwordResult = repository.changePassword(state.currentPassword, state.newPassword)
                if (passwordResult.isSuccess) {
                    // il backend ha chiuso tutte le sessioni: i token locali non valgono più
                    repository.logout()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentPassword = "",
                            newPassword = "",
                            confirmPassword = "",
                            successMessage = PASSWORD_CHANGED_MESSAGE,
                            passwordChanged = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, currentPasswordError = "Password attuale errata o non valida", errorMessage = "Password non aggiornata: verifica la password attuale") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, successMessage = "Profilo aggiornato", savedUser = profileResult.getOrThrow()) }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
            val result = repository.deleteAccount()
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isAccountDeleted = true) } },
                onFailure = { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage("Account non eliminato")) } }
            )
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(savedUser = null, isAccountDeleted = false, passwordChanged = false) }
    }

    companion object {
        const val PASSWORD_CHANGED_MESSAGE =
            "Password aggiornata con successo. Verrai disconnesso: accedi di nuovo con la nuova password."
    }
}
