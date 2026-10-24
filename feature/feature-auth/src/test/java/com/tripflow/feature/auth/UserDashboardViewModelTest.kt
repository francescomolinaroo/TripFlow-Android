package com.tripflow.feature.auth

import com.tripflow.core.model.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class UserDashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()

    @Test
    fun `getMe riuscito espone profilo in Success`() = runTest {
        repository.getMeResult = Result.success(sampleUser(firstName = "Luca"))
        val viewModel = UserDashboardViewModel(repository)
        viewModel.loadProfile()

        advanceUntilIdle()

        val profile = viewModel.uiState.value.profileState
        assertTrue(profile is UiState.Success)
        assertEquals("Luca", (profile as UiState.Success).data.firstName)
        assertFalse(viewModel.uiState.value.sessionExpired)
    }

    @Test
    fun `getMe fallito segnala sessione scaduta`() = runTest {
        repository.getMeResult = Result.failure(RuntimeException("401"))
        val viewModel = UserDashboardViewModel(repository)
        viewModel.loadProfile()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.profileState is UiState.Error)
        assertTrue(viewModel.uiState.value.sessionExpired)
    }

    @Test
    fun `logout chiama il repository e imposta isLoggedOut`() = runTest {
        repository.getMeResult = Result.success(sampleUser())
        val viewModel = UserDashboardViewModel(repository)
        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertTrue(repository.logoutCalled)
        assertTrue(viewModel.uiState.value.isLoggedOut)
    }

    @Test
    fun `rientrando nel profilo vengono mostrati i dati aggiornati`() = runTest {
        repository.getMeResult = Result.success(sampleUser(firstName = "Luca"))
        val viewModel = UserDashboardViewModel(repository)
        viewModel.loadProfile()
        advanceUntilIdle()

        repository.getMeResult = Result.success(sampleUser(firstName = "Marco"))
        viewModel.loadProfile()
        advanceUntilIdle()

        assertEquals("Marco", (viewModel.uiState.value.profileState as UiState.Success).data.firstName)
    }

    @Test
    fun `refresh fallito con sessione valida tiene i dati senza rimandare al login`() = runTest {
        repository.getMeResult = Result.success(sampleUser(firstName = "Luca"))
        val viewModel = UserDashboardViewModel(repository)
        viewModel.loadProfile()
        advanceUntilIdle()

        repository.hasSessionResult = true
        repository.getMeResult = Result.failure(java.io.IOException("rete assente"))
        viewModel.loadProfile()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.profileState is UiState.Success)
        assertFalse(viewModel.uiState.value.sessionExpired)
    }
}
