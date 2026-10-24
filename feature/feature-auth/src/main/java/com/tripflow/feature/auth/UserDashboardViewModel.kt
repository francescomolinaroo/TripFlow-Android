package com.tripflow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.model.UiState
import com.tripflow.core.network.auth.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserDashboardUiState(
    val profileState: UiState<UserResponse> = UiState.Loading,
    val isLoggedOut: Boolean = false,
    val sessionExpired: Boolean = false,
)

@HiltViewModel
class UserDashboardViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDashboardUiState())
    val uiState = _uiState.asStateFlow()

    // Chiamata a ogni ingresso nella schermata (non solo alla creazione), così dopo una modifica
    // del profilo si vedono i dati aggiornati. Se ci sono già dati, restano visibili durante il refresh.
    fun loadProfile() {
        viewModelScope.launch {
            if (_uiState.value.profileState !is UiState.Success) {
                _uiState.update { it.copy(profileState = UiState.Loading) }
            }
            val result = repository.getMe()
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(profileState = UiState.Success(user)) }
                },
                onFailure = { error ->
                    // refresh fallito per un errore temporaneo (rete, 5xx) con la sessione ancora
                    // valida: si tengono i dati già mostrati invece di rimandare al login
                    if (_uiState.value.profileState is UiState.Success && repository.hasSession()) {
                        return@fold
                    }
                    _uiState.update {
                        it.copy(
                            profileState = UiState.Error(error.message ?: "Profilo non disponibile"),
                            sessionExpired = true
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(isLoggedOut = false, sessionExpired = false) }
    }
}
