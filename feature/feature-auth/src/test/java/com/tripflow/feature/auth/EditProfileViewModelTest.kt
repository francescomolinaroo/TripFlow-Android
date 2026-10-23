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
        assertNotNull(viewModel.uiState.value.savedUser)
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
