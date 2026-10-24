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
class EditProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = EditProfileViewModel(repository)

    @Test
    fun `init popola lo stato dal profilo utente`() {
        viewModel.init(sampleUser(firstName = "Anna", lastName = "Bianchi", email = "anna@example.com"))

        val state = viewModel.uiState.value
        assertEquals("Anna", state.firstName)
        assertEquals("Bianchi", state.lastName)
        assertEquals("anna@example.com", state.email)
    }

    @Test
    fun `salvataggio riuscito senza password imposta savedUser`() = runTest {
        val updated = sampleUser(firstName = "Anna2")
        repository.updateProfileResult = Result.success(updated)
        viewModel.init(sampleUser(firstName = "Anna"))
        viewModel.onFirstNameChange("Anna2")

        viewModel.save()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.savedUser)
        assertNull(repository.changePasswordCalledWith)
    }

    @Test
    fun `salvataggio con nuova password chiama changePassword`() = runTest {
        repository.updateProfileResult = Result.success(sampleUser())
        repository.changePasswordResult = Result.success(Unit)
        viewModel.init(sampleUser())
        viewModel.onCurrentPasswordChange("oldpass1")
        viewModel.onNewPasswordChange("password8")
        viewModel.onConfirmPasswordChange("password8")

        viewModel.save()
        advanceUntilIdle()

        assertEquals("password8", repository.changePasswordCalledWith)
        assertEquals("", viewModel.uiState.value.currentPassword)
        assertEquals("", viewModel.uiState.value.newPassword)
    }

    @Test
    fun `cambio password riuscito avvisa e chiude la sessione`() = runTest {
        repository.updateProfileResult = Result.success(sampleUser())
        repository.changePasswordResult = Result.success(Unit)
        viewModel.init(sampleUser())
        viewModel.onCurrentPasswordChange("oldpass1")
        viewModel.onNewPasswordChange("password8")
        viewModel.onConfirmPasswordChange("password8")

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.passwordChanged)
        assertEquals(EditProfileViewModel.PASSWORD_CHANGED_MESSAGE, state.successMessage)
        assertTrue(repository.logoutCalled)
        // niente savedUser: la schermata deve tornare al login, non alla dashboard
        assertNull(state.savedUser)
    }

    @Test
    fun `cambio password fallito non chiude la sessione`() = runTest {
        repository.updateProfileResult = Result.success(sampleUser())
        repository.changePasswordResult = Result.failure(RuntimeException("400"))
        viewModel.init(sampleUser())
        viewModel.onCurrentPasswordChange("sbagliata")
        viewModel.onNewPasswordChange("password8")
        viewModel.onConfirmPasswordChange("password8")

        viewModel.save()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.passwordChanged)
        assertFalse(repository.logoutCalled)
        assertNotNull(viewModel.uiState.value.currentPasswordError)
    }

    @Test
    fun `riaprire la schermata scarta le modifiche non salvate`() {
        viewModel.init(sampleUser(firstName = "Anna"))
        viewModel.onFirstNameChange("Bozza")
        viewModel.onCurrentPasswordChange("oldpass1")

        viewModel.init(sampleUser(firstName = "Anna"))

        assertEquals("Anna", viewModel.uiState.value.firstName)
        assertEquals("", viewModel.uiState.value.currentPassword)
    }

    @Test
    fun `errore HTTP mostra un messaggio leggibile`() = runTest {
        val risposta = retrofit2.Response.error<Any>(
            400, okhttp3.ResponseBody.create(null, "{}")
        )
        repository.updateProfileResult = Result.failure(retrofit2.HttpException(risposta))
        viewModel.init(sampleUser())

        viewModel.save()
        advanceUntilIdle()

        assertEquals("Dati non validi: controlla i campi inseriti", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `data di nascita gia impostata non si puo modificare`() {
        viewModel.init(sampleUser(dateOfBirth = "1990-05-12"))

        viewModel.onDateOfBirthChange("1991-01-01")

        assertTrue(viewModel.uiState.value.dateOfBirthLocked)
        assertEquals("1990-05-12", viewModel.uiState.value.dateOfBirth)
    }

    @Test
    fun `data di nascita inesistente o futura e non valida`() {
        viewModel.init(sampleUser())

        viewModel.onDateOfBirthChange("2023-02-31")
        assertNotNull(viewModel.uiState.value.dateOfBirthError)

        viewModel.onDateOfBirthChange(java.time.LocalDate.now().plusDays(1).toString())
        assertNotNull(viewModel.uiState.value.dateOfBirthError)

        viewModel.onDateOfBirthChange("1990-05-12")
        assertNull(viewModel.uiState.value.dateOfBirthError)
    }

    @Test
    fun `eliminazione account riuscita imposta isAccountDeleted`() = runTest {
        repository.deleteAccountResult = Result.success(Unit)
        viewModel.init(sampleUser())

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAccountDeleted)
    }

    @Test
    fun `nome vuoto blocca il salvataggio`() = runTest {
        viewModel.init(sampleUser())
        viewModel.onFirstNameChange("")

        viewModel.save()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.savedUser)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }
}
