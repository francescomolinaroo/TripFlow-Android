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
class ResetPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = ResetPasswordViewModel(repository)

    @Test
    fun `reset valido imposta isCompleted e invia email codice e password`() = runTest {
        repository.resetPasswordResult = Result.success(Unit)
        viewModel.setEmail("mario@example.com")
        viewModel.onCodeChange("123456")
        viewModel.onNewPasswordChange("password8")
        viewModel.onConfirmPasswordChange("password8")

        viewModel.resetPassword()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCompleted)
        assertEquals(Triple("mario@example.com", "123456", "password8"), repository.resetPasswordCalledWith)
    }

    @Test
    fun `codice incompleto blocca il reset`() = runTest {
        viewModel.setEmail("mario@example.com")
        viewModel.onCodeChange("12")
        viewModel.onNewPasswordChange("password8")
        viewModel.onConfirmPasswordChange("password8")

        viewModel.resetPassword()
        advanceUntilIdle()

        assertNull(repository.resetPasswordCalledWith)
        assertNotNull(viewModel.uiState.value.codeError)
    }

    @Test
    fun `password non corrispondenti bloccano il reset`() = runTest {
        viewModel.setEmail("mario@example.com")
        viewModel.onCodeChange("123456")
        viewModel.onNewPasswordChange("password8")
        viewModel.onConfirmPasswordChange("diversa9")

        viewModel.resetPassword()
        advanceUntilIdle()

        assertNull(repository.resetPasswordCalledWith)
        assertNotNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `reset fallito imposta errorMessage`() = runTest {
        repository.resetPasswordResult = Result.failure(RuntimeException("400"))
        viewModel.setEmail("mario@example.com")
        viewModel.onCodeChange("123456")
        viewModel.onNewPasswordChange("password8")
        viewModel.onConfirmPasswordChange("password8")

        viewModel.resetPassword()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCompleted)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }
}
