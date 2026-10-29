package com.tripflow.feature.itinerary.ui.addstage

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.MainDispatcherRule
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.StopType
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddStopViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initialize loads itinerary context and next order index`() = runTest {
        val viewModel = AddStopViewModel(FakeItineraryRepository())

        viewModel.initialize("1")

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoadingContext)
        assertNull(state.contextError)
        assertEquals(java.time.LocalDate.of(2026, 9, 14), state.date)
        assertEquals(3, state.orderIndex)
    }

    @Test
    fun `addStop validates required custom title`() = runTest {
        val viewModel = AddStopViewModel(FakeItineraryRepository())
        viewModel.initialize("1")

        viewModel.addStop()

        assertEquals("Inserisci un titolo per la tappa", viewModel.uiState.value.customTitleError)
        assertNull(viewModel.uiState.value.addedStageId)
    }

    @Test
    fun `addStop validates time range`() = runTest {
        val viewModel = AddStopViewModel(FakeItineraryRepository())
        viewModel.initialize("1")
        viewModel.onCustomTitleChange("Colazione")
        viewModel.onStartTimeChange("11:00")
        viewModel.onEndTimeChange("10:00")

        viewModel.addStop()

        assertEquals(
            "L'orario di fine deve essere successivo a quello di inizio",
            viewModel.uiState.value.timeError,
        )
        assertNull(viewModel.uiState.value.addedStageId)
    }

    @Test
    fun `addStop submits custom stop`() = runTest {
        val viewModel = AddStopViewModel(FakeItineraryRepository())
        viewModel.initialize("1")
        viewModel.onCustomTitleChange("Colazione al mercato")
        viewModel.onCustomDescriptionChange("Prodotti tipici")
        viewModel.onNotesChange("Prenotare il tavolo")

        viewModel.addStop()

        val state = viewModel.uiState.value
        assertNotNull(state.addedStageId)
        assertEquals(false, state.isSubmitting)
        assertNull(state.errorMessage)
    }

    @Test
    fun `addStop requires catalog selection for trip`() = runTest {
        val viewModel = AddStopViewModel(FakeItineraryRepository())
        viewModel.initialize("1")
        viewModel.onStopTypeChange(StopType.TRIP)

        viewModel.addStop()

        assertEquals("Seleziona un elemento dal catalogo", viewModel.uiState.value.catalogError)
        assertNull(viewModel.uiState.value.addedStageId)
    }

    @Test
    fun `searchCatalog loads trips and selected item can be submitted`() = runTest {
        val viewModel = AddStopViewModel(FakeItineraryRepository())
        viewModel.initialize("1")
        viewModel.onStopTypeChange(StopType.TRIP)
        viewModel.onCatalogQueryChange("Etna")

        viewModel.searchCatalog()
        val item = viewModel.uiState.value.catalogResults.first()

        viewModel.selectCatalogItem(item)
        viewModel.addStop()

        assertNotNull(viewModel.uiState.value.addedStageId)
        assertNull(viewModel.uiState.value.catalogError)
    }

    @Test
    fun `searchActivities loads activity results`() = runTest {
        val viewModel = AddStopViewModel(FakeItineraryRepository())
        viewModel.initialize("1")
        viewModel.onStopTypeChange(StopType.ACTIVITY)
        viewModel.onCatalogQueryChange("barca")

        viewModel.searchCatalog()

        val results = viewModel.uiState.value.catalogResults
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.title.contains("barca", ignoreCase = true) })
    }

    @Test
    fun `order conflict proposes next position`() = runTest {
        val repository = object : ItineraryRepository by FakeItineraryRepository() {
            override suspend fun addStage(
                itineraryId: String,
                request: CreateStageRequest,
            ): UiState<StageDetail> = UiState.Error("Conflitto: posizione già occupata")
        }
        val viewModel = AddStopViewModel(repository)
        viewModel.initialize("1")
        viewModel.onCustomTitleChange("Tappa")

        viewModel.addStop()

        val state = viewModel.uiState.value
        assertEquals(4, state.orderIndex)
        assertEquals(false, state.isSubmitting)
        assertNull(state.addedStageId)
        assertTrue(state.errorMessage.orEmpty().contains("posizione 4"))
    }

    @Test
    fun `initialize retry clears context error and loads dates`() = runTest {
        val fake = FakeItineraryRepository()
        var shouldFail = true
        val repository = object : ItineraryRepository by fake {
            override suspend fun getItineraryDetail(id: String): UiState<ItineraryDetail> {
                if (shouldFail) {
                    shouldFail = false
                    return UiState.Error("Rete non disponibile", retryable = false)
                }
                return fake.getItineraryDetail(id)
            }
        }
        val viewModel = AddStopViewModel(repository)

        viewModel.initialize("1")
        val errorState = viewModel.uiState.value
        assertEquals("Rete non disponibile", errorState.contextError)
        assertEquals(false, errorState.contextErrorRetryable)

        viewModel.initialize("1")

        val state = viewModel.uiState.value
        assertNull(state.contextError)
        assertEquals(true, state.contextErrorRetryable)
        assertEquals(false, state.isLoadingContext)
        assertEquals(java.time.LocalDate.of(2026, 9, 14), state.itineraryStartDate)
    }

    @Test
    fun `addStop retry clears submission error after success`() = runTest {
        val fake = FakeItineraryRepository()
        var shouldFail = true
        val repository = object : ItineraryRepository by fake {
            override suspend fun addStage(
                itineraryId: String,
                request: CreateStageRequest,
            ): UiState<StageDetail> {
                if (shouldFail) {
                    shouldFail = false
                    return UiState.Error("Servizio non disponibile")
                }
                return fake.addStage(itineraryId, request)
            }
        }
        val viewModel = AddStopViewModel(repository)
        viewModel.initialize("1")
        viewModel.onCustomTitleChange("Colazione")

        viewModel.addStop()
        assertEquals("Servizio non disponibile", viewModel.uiState.value.errorMessage)

        viewModel.addStop()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.addedStageId)
        assertEquals(false, state.isSubmitting)
    }

    @Test
    fun `addStop rejects a date outside the itinerary period`() = runTest {
        val viewModel = AddStopViewModel(FakeItineraryRepository())
        viewModel.initialize("1")
        viewModel.onCustomTitleChange("Fuori periodo")
        viewModel.onDateChange(java.time.LocalDate.of(2026, 9, 20))

        viewModel.addStop()

        assertEquals("La data deve essere compresa nel periodo dell'itinerario", viewModel.uiState.value.dateError)
    }
}
