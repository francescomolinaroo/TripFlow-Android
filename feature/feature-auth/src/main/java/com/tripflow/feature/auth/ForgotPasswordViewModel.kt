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

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false,
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    // Chiamata a ogni ingresso nella schermata, che altrimenti riaprirebbe già in stato "inviato".
    fun reset() {
        _uiState.value = ForgotPasswordUiState()
    }

    fun onEmailChange(value: String) {
        val error = if (value.isNotEmpty() && !isEmailValid(value))
            "Inserisci un'email valida (es. nome@esempio.it)" else null
        _uiState.update { it.copy(email = value, emailError = error, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        if (!isEmailValid(state.email)) {
            _uiState.update { it.copy(emailError = "Inserisci un indirizzo email valido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.forgotPassword(state.email.trim())
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSubmitted = true) } },
                onFailure = { _uiState.update { it.copy(isLoading = false, errorMessage = "Impossibile inviare la richiesta. Riprova più tardi.") } }
            )
        }
    }
}
