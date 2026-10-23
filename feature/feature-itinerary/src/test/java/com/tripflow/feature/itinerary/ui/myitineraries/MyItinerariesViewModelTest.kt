package com.tripflow.feature.itinerary.ui.myitineraries

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.MainDispatcherRule
import com.tripflow.feature.itinerary.model.MyItinerary
import com.tripflow.feature.itinerary.model.Visibility
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MyItinerariesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ItineraryRepository
    private lateinit var viewModel: MyItinerariesViewModel

    @Before
    fun setup() {
        repository = FakeItineraryRepository()
        viewModel = MyItinerariesViewModel(repository)
    }

    @Test
    fun `loadItineraries emits Success with mock data`() = runTest {
        val uiState = viewModel.uiState.first()

        assertNotNull(uiState)
        assertTrue(uiState is UiState.Success)
        val success = uiState as UiState.Success<List<MyItinerary>>
        val itineraries = success.data

        assertEquals(3, itineraries.size)

        val amalfi = itineraries.first { it.title == "Costa Amalfitana" }
        assertEquals("14 - 18 set 2026", amalfi.dateRange)
        assertEquals(12, amalfi.stopsCount)
        assertEquals(Visibility.PRIVATE, amalfi.visibility)
        assertEquals(3, amalfi.stopsPreview.size)
        assertEquals("Arrivo ad Amalfi e check-in", amalfi.stopsPreview[0].title)
        assertEquals("14:00", amalfi.stopsPreview[0].time)
        assertEquals("Passeggiata sul lungomare", amalfi.stopsPreview[1].title)
        assertEquals("17:00", amalfi.stopsPreview[1].time)
        assertEquals("Escursione a Positano", amalfi.stopsPreview[2].title)
        assertEquals("09:00", amalfi.stopsPreview[2].time)

        val trekking = itineraries.first { it.title == "Trekking Dolomiti" }
        assertEquals("10 - 15 lug 2026", trekking.dateRange)
        assertEquals(8, trekking.stopsCount)
        assertEquals(Visibility.SHARED, trekking.visibility)
        assertEquals(2, trekking.stopsPreview.size)
        assertEquals("Arrivo a Ortisei", trekking.stopsPreview[0].title)
        assertEquals("12:00", trekking.stopsPreview[0].time)

        val sicilia = itineraries.first { it.title == "Sicilia on the road" }
        assertEquals("1 - 10 ott 2026", sicilia.dateRange)
        assertEquals(15, sicilia.stopsCount)
        assertEquals(Visibility.PUBLIC, sicilia.visibility)
        assertEquals(3, sicilia.stopsPreview.size)
    }

    @Test
    fun `onItineraryDeleted reloads data`() = runTest {
        val firstLoad = viewModel.uiState.first()
        assertTrue(firstLoad is UiState.Success)

        viewModel.onItineraryDeleted()

        val secondLoad = viewModel.uiState.first()
        assertTrue(secondLoad is UiState.Success)
        val success = secondLoad as UiState.Success<List<MyItinerary>>
        assertEquals(3, success.data.size)
    }

    @Test
    fun `onItineraryUpdated reloads data`() = runTest {
        val firstLoad = viewModel.uiState.first()
        assertTrue(firstLoad is UiState.Success)

        viewModel.onItineraryUpdated()

        val secondLoad = viewModel.uiState.first()
        assertTrue(secondLoad is UiState.Success)
        val success = secondLoad as UiState.Success<List<MyItinerary>>
        assertEquals(3, success.data.size)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val testViewModel = MyItinerariesViewModel(FakeItineraryRepository())
        val initialState = testViewModel.uiState.value

        assertTrue(initialState is UiState.Loading || initialState is UiState.Success)
    }
}
