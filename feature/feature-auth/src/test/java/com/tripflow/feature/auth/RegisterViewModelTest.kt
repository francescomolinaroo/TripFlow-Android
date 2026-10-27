package com.tripflow.feature.auth

import com.tripflow.core.network.auth.RegisterResponse
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
import java.util.UUID

@ExperimentalCoroutinesApi
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = RegisterViewModel(repository)

    private fun fillValidForm() {
        viewModel.onFirstNameChange("Mario")
        viewModel.onLastNameChange("Rossi")
        viewModel.onEmailChange("mario@example.com")
        viewModel.onPasswordChange("password8")
        viewModel.onConfirmPasswordChange("password8")
    }

    @Test
    fun `registrazione valida imposta isRegistered e invia la request`() = runTest {
        repository.registerResult = Result.success(RegisterResponse(UUID.randomUUID(), "mario@example.com", true))
        fillValidForm()

        viewModel.register()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isRegistered)
        assertEquals("TRAVELER", repository.registerCalledWith?.role)
    }

    @Test
    fun `telefono con prefisso predefinito inviato in formato E164`() = runTest {
        repository.registerResult = Result.success(RegisterResponse(UUID.randomUUID(), "mario@example.com", true))
        fillValidForm()
        assertEquals("+39", viewModel.uiState.value.phonePrefix)

        viewModel.onPhoneNumberChange("+39 333-123 4567x")
        assertEquals("393331234567", viewModel.uiState.value.phoneNumber)
        viewModel.onPhoneNumberChange("333 123 4567")
        viewModel.register()
        advanceUntilIdle()

        assertEquals("+393331234567", repository.registerCalledWith?.phoneNumber)
    }

    @Test
    fun `telefono vuoto non viene inviato, prefisso scelto viene usato`() = runTest {
        repository.registerResult = Result.success(RegisterResponse(UUID.randomUUID(), "mario@example.com", true))
        fillValidForm()
        viewModel.register()
        advanceUntilIdle()
        assertNull(repository.registerCalledWith?.phoneNumber)

        viewModel.onPhonePrefixChange("+33")
        viewModel.onPhoneNumberChange("612345678")
        viewModel.register()
        advanceUntilIdle()
        assertEquals("+33612345678", repository.registerCalledWith?.phoneNumber)
    }

    @Test
    fun `numero troppo corto blocca la registrazione`() = runTest {
        fillValidForm()

        viewModel.onPhoneNumberChange("12345")
        viewModel.register()
        advanceUntilIdle()

        assertEquals(ERRORE_NUMERO, viewModel.uiState.value.phoneError)
        assertNull(repository.registerCalledWith)
    }

    @Test
    fun `dopo la navigazione il form viene svuotato`() = runTest {
        repository.registerResult = Result.success(RegisterResponse(UUID.randomUUID(), "mario@example.com", true))
        fillValidForm()
        viewModel.register()
        advanceUntilIdle()

        viewModel.onNavigated()

        assertFalse(viewModel.uiState.value.isRegistered)
        assertEquals("", viewModel.uiState.value.email)
        assertEquals("", viewModel.uiState.value.password)
    }

    @Test
    fun `errore 409 produce messaggio su email gia registrata`() = runTest {
        repository.registerResult = Result.failure(RuntimeException("HTTP 409"))
        fillValidForm()

        viewModel.register()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRegistered)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("già registrata"))
    }

    @Test
    fun `form non valido non chiama il repository`() = runTest {
        viewModel.onFirstNameChange("Mario")
        viewModel.onEmailChange("non-valida")

        viewModel.register()
        advanceUntilIdle()

        assertNull(repository.registerCalledWith)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `password non corrispondenti segnala errore di conferma`() {
        viewModel.onPasswordChange("password8")
        viewModel.onConfirmPasswordChange("diversa9")

        assertNotNull(viewModel.uiState.value.confirmPasswordError)
    }
}
