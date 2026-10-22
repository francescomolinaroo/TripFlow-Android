package com.tripflow.feature.itinerary.ui.createitinerary

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.MainDispatcherRule
import com.tripflow.feature.itinerary.model.CreateItineraryRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.Visibility
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateItineraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeItineraryRepository()
    private val viewModel = CreateItineraryViewModel(repository)

    @Test
    fun `createItinerary rejects blank title before calling backend`() = runTest {
        viewModel.onStartDateChange(LocalDate.of(2026, 11, 1))
        viewModel.onEndDateChange(LocalDate.of(2026, 11, 5))

        viewModel.createItinerary()

        assertEquals("Inserisci un titolo per l'itinerario", viewModel.uiState.value.titleError)
        assertNull(viewModel.uiState.value.createdItineraryId)
    }

    @Test
    fun `createItinerary rejects end date before start date`() = runTest {
        viewModel.onTitleChange("Weekend a Roma")
        viewModel.onStartDateChange(LocalDate.of(2026, 11, 5))
        viewModel.onEndDateChange(LocalDate.of(2026, 11, 1))

        viewModel.createItinerary()

        assertEquals(
            "La data di fine non può precedere quella di inizio",
            viewModel.uiState.value.dateError,
        )
        assertNull(viewModel.uiState.value.createdItineraryId)
    }

    @Test
    fun `createItinerary submits valid data and exposes created id`() = runTest {
        viewModel.onTitleChange("Weekend a Roma")
        viewModel.onDescriptionChange("Tre giorni tra storia e cibo")
        viewModel.onStartDateChange(LocalDate.of(2026, 11, 1))
        viewModel.onEndDateChange(LocalDate.of(2026, 11, 5))
        viewModel.onVisibilityChange(Visibility.PUBLIC)

        viewModel.createItinerary()

        val state = viewModel.uiState.value
        assertNotNull(state.createdItineraryId)
        assertEquals(false, state.isSubmitting)
        assertNull(state.errorMessage)

        val created = repository.getItineraryDetail(requireNotNull(state.createdItineraryId)) as UiState.Success
        assertEquals("Weekend a Roma", created.data.title)
        assertEquals(Visibility.PUBLIC, created.data.visibility)
    }

    @Test
    fun `startNewCreation resets form`() = runTest {
        viewModel.onTitleChange("Bozza")
        viewModel.onStartDateChange(LocalDate.of(2026, 11, 1))

        viewModel.startNewCreation()

        assertEquals(CreateItineraryUiState(), viewModel.uiState.value)
    }

    @Test
    fun `createItinerary retry clears error after success`() = runTest {
        val fake = FakeItineraryRepository()
        var shouldFail = true
        val retryRepository = object : ItineraryRepository by fake {
            override suspend fun createItinerary(
                request: CreateItineraryRequest,
            ): UiState<ItineraryDetail> {
                if (shouldFail) {
                    shouldFail = false
                    return UiState.Error("Servizio non disponibile")
                }
                return fake.createItinerary(request)
            }
        }
        val retryViewModel = CreateItineraryViewModel(retryRepository)
        retryViewModel.onTitleChange("Weekend a Roma")
        retryViewModel.onStartDateChange(LocalDate.of(2026, 11, 1))
        retryViewModel.onEndDateChange(LocalDate.of(2026, 11, 5))

        retryViewModel.createItinerary()
        assertEquals("Servizio non disponibile", retryViewModel.uiState.value.errorMessage)

        retryViewModel.createItinerary()

        val state = retryViewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.createdItineraryId)
        assertEquals(false, state.isSubmitting)
    }
}
