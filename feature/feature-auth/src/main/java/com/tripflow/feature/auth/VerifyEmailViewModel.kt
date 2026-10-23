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

data class VerifyEmailUiState(
    val email: String = "",
    val code: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isVerified: Boolean = false,
)

// Verifica dell'email tramite codice OTP ricevuto dopo la registrazione.
@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyEmailUiState())
    val uiState = _uiState.asStateFlow()

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onCodeChange(value: String) {
        val filtrato = value.filter { it.isDigit() }.take(6)
        val error = if (filtrato.isNotEmpty() && filtrato.length < 6) "Il codice è di 6 cifre" else null
        _uiState.update { it.copy(code = filtrato, codeError = error, errorMessage = null) }
    }

    fun verify() {
        val state = _uiState.value
        if (state.isLoading) return

        if (state.code.length != 6) {
            _uiState.update { it.copy(codeError = "Inserisci il codice di 6 cifre") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val result = repository.verifyEmail(state.email.trim(), state.code)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isVerified = true) } },
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

    fun resend() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val result = repository.resendVerification(state.email.trim())
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, infoMessage = "Ti abbiamo inviato un nuovo codice.") } },
                onFailure = { _uiState.update { it.copy(isLoading = false, errorMessage = "Impossibile reinviare il codice. Riprova più tardi.") } }
            )
        }
    }
}
