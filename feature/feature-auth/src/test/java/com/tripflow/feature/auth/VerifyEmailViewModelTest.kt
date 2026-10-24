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
class VerifyEmailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = VerifyEmailViewModel(repository)

    @Test
    fun `verifica con codice valido imposta isVerified e invia email piu codice`() = runTest {
        repository.verifyEmailResult = Result.success(Unit)
        viewModel.setEmail("mario@example.com")
        viewModel.onCodeChange("123456")

        viewModel.verify()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isVerified)
        assertEquals("mario@example.com" to "123456", repository.verifyEmailCalledWith)
    }

    @Test
    fun `rientrare nella schermata azzera lo stato della verifica precedente`() = runTest {
        repository.verifyEmailResult = Result.success(Unit)
        viewModel.setEmail("primo@example.com")
        viewModel.onCodeChange("123456")
        viewModel.verify()
        advanceUntilIdle()

        viewModel.setEmail("secondo@example.com")

        // altrimenti la schermata richiamerebbe subito onVerified senza codice
        assertFalse(viewModel.uiState.value.isVerified)
        assertEquals("", viewModel.uiState.value.code)
        assertEquals("secondo@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun `codice incompleto non chiama il repository`() = runTest {
        viewModel.setEmail("mario@example.com")
        viewModel.onCodeChange("123")

        viewModel.verify()
        advanceUntilIdle()

        assertNull(repository.verifyEmailCalledWith)
        assertNotNull(viewModel.uiState.value.codeError)
    }

    @Test
    fun `onCodeChange tiene solo cifre e massimo 6`() {
        viewModel.onCodeChange("12ab34cd5678")

        assertEquals("123456", viewModel.uiState.value.code)
    }

    @Test
    fun `verifica fallita imposta errorMessage`() = runTest {
        repository.verifyEmailResult = Result.failure(RuntimeException("400"))
        viewModel.setEmail("mario@example.com")
        viewModel.onCodeChange("123456")

        viewModel.verify()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isVerified)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `resend riuscito imposta infoMessage`() = runTest {
        repository.resendVerificationResult = Result.success(Unit)
        viewModel.setEmail("mario@example.com")

        viewModel.resend()
        advanceUntilIdle()

        assertEquals("mario@example.com", repository.resendVerificationCalledWith)
        assertNotNull(viewModel.uiState.value.infoMessage)
    }
}
