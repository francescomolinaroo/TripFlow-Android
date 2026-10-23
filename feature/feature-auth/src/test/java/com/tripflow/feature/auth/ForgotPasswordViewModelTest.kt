package com.tripflow.feature.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = ForgotPasswordViewModel(repository)

    @Test
    fun `invio con email valida imposta isSubmitted`() = runTest {
        repository.forgotPasswordResult = Result.success(Unit)
        viewModel.onEmailChange("mario@example.com")

        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSubmitted)
        assertEquals("mario@example.com", repository.forgotPasswordCalledWith)
    }

    @Test
    fun `email non valida non invia la richiesta`() = runTest {
        viewModel.onEmailChange("non-valida")

        viewModel.submit()
        advanceUntilIdle()

        assertNull(repository.forgotPasswordCalledWith)
        assertFalse(viewModel.uiState.value.isSubmitted)
        assertNotNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `fallimento imposta errorMessage`() = runTest {
        repository.forgotPasswordResult = Result.failure(RuntimeException("500"))
        viewModel.onEmailChange("mario@example.com")

        viewModel.submit()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitted)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }
}
