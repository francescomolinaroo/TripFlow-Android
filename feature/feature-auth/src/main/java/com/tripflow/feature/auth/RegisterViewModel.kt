package com.tripflow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val selectedRole: String = "TRAVELER",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val phoneError: String? = null,
    val dateOfBirthError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) {
        _uiState.update {
            it.copy(
                firstName = value,
                firstNameError = if (value.isNotEmpty() && value.isBlank()) "Il nome non può essere solo spazi" else null
            )
        }
    }

    fun onLastNameChange(value: String) {
        _uiState.update {
            it.copy(
                lastName = value,
                lastNameError = if (value.isNotEmpty() && value.isBlank()) "Il cognome non può essere solo spazi" else null
            )
        }
    }

    fun onEmailChange(value: String) {
        val error = if (value.isEmpty()) null
        else if (!isEmailValid(value)) "Inserisci un'email valida (es. nome@esempio.it)"
        else null
        _uiState.update { it.copy(email = value, emailError = error) }
    }

    fun onPasswordChange(value: String) {
        val error = if (value.isEmpty()) null
        else if (!isPasswordValidForRegister(value)) "La password deve essere di almeno 8 caratteri"
        else null
        val confirmError = if (_uiState.value.confirmPassword.isEmpty()) null
        else if (_uiState.value.confirmPassword != value) "Le password non corrispondono"
        else null
        _uiState.update { it.copy(password = value, passwordError = error, confirmPasswordError = confirmError) }
    }

    fun onConfirmPasswordChange(value: String) {
        val error = if (value.isEmpty()) null
        else if (value != _uiState.value.password) "Le password non corrispondono"
        else null
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = error) }
    }

    fun onPhoneNumberChange(value: String) {
        val error = if (value.isEmpty()) null
        else if (!isPhoneValid(value)) "Numero di telefono non valido"
        else null
        _uiState.update { it.copy(phoneNumber = value, phoneError = error) }
    }

    fun onDateOfBirthChange(value: String) {
        val error = if (value.isEmpty()) null
        else if (!isDateOfBirthValid(value)) "Formato non valido. Usa aaaa-mm-gg"
        else null
        _uiState.update { it.copy(dateOfBirth = value, dateOfBirthError = error) }
    }

    fun onRoleChange(role: String) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun register() {
        val state = _uiState.value
        if (state.isLoading) return

        val isValid = state.firstName.isNotBlank() &&
                state.lastName.isNotBlank() &&
                isEmailValid(state.email) &&
                isPasswordValidForRegister(state.password) &&
                state.confirmPassword == state.password &&
                (state.dateOfBirth.isEmpty() || isDateOfBirthValid(state.dateOfBirth)) &&
                (state.phoneNumber.isEmpty() || isPhoneValid(state.phoneNumber))

        if (!isValid) {
            _uiState.update { it.copy(errorMessage = "Correggi i campi evidenziati in rosso prima di procedere.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.register(
                RegisterRequest(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    password = state.password,
                    role = state.selectedRole,
                    dateOfBirth = state.dateOfBirth.ifBlank { null },
                    phoneNumber = state.phoneNumber.ifBlank { null }
                )
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isRegistered = true) } },
                onFailure = { error ->
                    val errorMsg = error.message.orEmpty()
                    val msg = when {
                        errorMsg.contains("409") -> "Questa email è già registrata. Accedi con le tue credenziali."
                        errorMsg.contains("429") -> "Troppe richieste in poco tempo. Riprova tra un minuto."
                        else -> "Registrazione non riuscita. Controlla i dati inseriti."
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
            )
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(isRegistered = false) }
    }
}
