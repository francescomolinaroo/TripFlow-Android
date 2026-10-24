package com.tripflow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordUiState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val codeError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false,
)

// Reset password con codice OTP: l'utente inserisce il codice ricevuto via email
// (dallo schermo precedente arriva solo l'email) insieme alla nuova password.
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    // Chiamata a ogni ingresso nella schermata: azzera codice, password e isCompleted del giro precedente.
    fun setEmail(email: String) {
        _uiState.value = ResetPasswordUiState(email = email)
    }

    fun onCodeChange(value: String) {
        // solo cifre, max 6
        val filtrato = value.filter { it.isDigit() }.take(6)
        val error = if (filtrato.isNotEmpty() && filtrato.length < 6) "Il codice è di 6 cifre" else null
        _uiState.update { it.copy(code = filtrato, codeError = error, errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        val error = if (value.isNotEmpty() && !isPasswordValidForRegister(value))
            "La password deve essere di almeno 8 caratteri" else null
        val confirmError = if (_uiState.value.confirmPassword.isNotEmpty() && _uiState.value.confirmPassword != value)
            "Le password non corrispondono" else null
        _uiState.update { it.copy(newPassword = value, newPasswordError = error, confirmPasswordError = confirmError, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        val error = if (value.isNotEmpty() && value != _uiState.value.newPassword)
            "Le password non corrispondono" else null
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = error, errorMessage = null) }
    }

    fun resetPassword() {
        val state = _uiState.value
        if (state.isLoading) return

        val codeOk = state.code.length == 6
        val passwordOk = isPasswordValidForRegister(state.newPassword)
        val confirmOk = state.confirmPassword == state.newPassword

        if (!codeOk || !passwordOk || !confirmOk) {
            _uiState.update {
                it.copy(
                    codeError = if (!codeOk) "Inserisci il codice di 6 cifre" else null,
                    newPasswordError = if (!passwordOk) "La password deve essere di almeno 8 caratteri" else null,
                    confirmPasswordError = if (!confirmOk) "Le password non corrispondono" else null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.resetPassword(state.email.trim(), state.code, state.newPassword)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isCompleted = true) } },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Codice non valido o scaduto. Controlla la mail o richiedi un nuovo codice."
                        )
                    }
                }
            )
        }
    }
}
