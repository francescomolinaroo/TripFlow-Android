package com.tripflow.feature.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
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
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()
    private var initialized = false

    fun init(user: UserResponse) {
        if (initialized) return
        initialized = true
        _uiState.update {
            it.copy(
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                dateOfBirth = user.dateOfBirth.orEmpty(),
                phoneNumber = user.phoneNumber.orEmpty(),
                profileImage = user.profileImage.orEmpty(),
            )
        }
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
        val error = if (!isDateOfBirthValid(value)) "Formato non valido: usa AAAA-MM-GG" else null
        _uiState.update { it.copy(dateOfBirth = value, dateOfBirthError = error) }
    }

    fun onPhoneNumberChange(value: String) {
        val error = if (value.isNotBlank() && value.length !in 7..20) "Numero di telefono non valido (7-20 cifre)" else null
        _uiState.update { it.copy(phoneNumber = value, phoneError = error) }
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
                onFailure = { error -> _uiState.update { it.copy(isUploadingAvatar = false, errorMessage = error.message ?: "Errore durante il caricamento dell'immagine") } }
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
                    phoneNumber = state.phoneNumber.ifBlank { null },
                    profileImage = state.profileImage.ifBlank { null }
                )
            )
            if (profileResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, errorMessage = profileResult.exceptionOrNull()?.message ?: "Profilo non aggiornato") }
                return@launch
            }
            if (state.newPassword.isNotBlank()) {
                val passwordResult = repository.changePassword(state.currentPassword, state.newPassword)
                if (passwordResult.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Profilo e password aggiornati", savedUser = profileResult.getOrThrow()) }
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
                onFailure = { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Account non eliminato") } }
            )
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(savedUser = null, isAccountDeleted = false) }
    }
}
