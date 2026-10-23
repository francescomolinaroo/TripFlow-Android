package com.tripflow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.auth.EmailNotVerifiedException
import com.tripflow.core.network.auth.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: UserResponse? = null,
    // valorizzata quando il login è rifiutato perché l'email non è ancora verificata (403)
    val needsVerificationEmail: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        val error = if (value.isNotEmpty() && !isEmailValid(value))
            "Inserisci un'email valida (es. nome@esempio.it)" else null
        _uiState.update { it.copy(email = value, emailError = error, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        val error = if (value.isNotEmpty() && !isPasswordValidForLogin(value))
            "La password deve essere di almeno 6 caratteri" else null
        _uiState.update { it.copy(password = value, passwordError = error, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailOk = isEmailValid(state.email)
        val passwordOk = isPasswordValidForLogin(state.password)

        if (!emailOk || !passwordOk) {
            _uiState.update {
                it.copy(
                    emailError = if (!emailOk) "Inserisci un'email valida" else it.emailError,
                    passwordError = if (!passwordOk) "La password deve essere di almeno 6 caratteri" else it.passwordError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.login(state.email.trim(), state.password)
            result.fold(
                onSuccess = { user -> _uiState.update { it.copy(isLoading = false, loggedInUser = user) } },
                onFailure = { error ->
                    // email non verificata: l'utente va mandato alla schermata di verifica,
                    // non trattato come credenziali errate
                    if (error is EmailNotVerifiedException) {
                        _uiState.update { it.copy(isLoading = false, needsVerificationEmail = error.email) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Accesso non riuscito. Controlla email e password.") }
                    }
                }
            )
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(loggedInUser = null, needsVerificationEmail = null) }
    }
}
