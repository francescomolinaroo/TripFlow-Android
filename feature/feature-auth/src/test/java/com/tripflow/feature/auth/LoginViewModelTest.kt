package com.tripflow.feature.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = LoginViewModel(repository)

    @Test
    fun `login con credenziali valide imposta loggedInUser`() = runTest {
        repository.loginResult = Result.success(sampleUser())
        viewModel.onEmailChange("mario@example.com")
        viewModel.onPasswordChange("password")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.loggedInUser)
        assertEquals("mario@example.com", repository.loginCalledWith?.first)
        assertNull(state.errorMessage)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `login fallito imposta errorMessage`() = runTest {
        repository.loginResult = Result.failure(RuntimeException("401"))
        viewModel.onEmailChange("mario@example.com")
        viewModel.onPasswordChange("password")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.loggedInUser)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `input non valido non chiama il repository`() = runTest {
        viewModel.onEmailChange("non-una-email")
        viewModel.onPasswordChange("123")

        viewModel.login()
        advanceUntilIdle()

        assertNull(repository.loginCalledWith)
        assertNotNull(viewModel.uiState.value.emailError)
        assertNotNull(viewModel.uiState.value.passwordError)
    }
}
