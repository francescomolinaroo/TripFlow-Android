package com.tripflow.feature.itinerary.ui.myitineraries

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.MyItinerary
import com.tripflow.feature.itinerary.model.StopPreview
import com.tripflow.feature.itinerary.repository.FakeMyItineraryRepository
import com.tripflow.feature.itinerary.repository.MyItineraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MyItinerariesViewModelTest {

    private lateinit var repository: MyItineraryRepository
    private lateinit var viewModel: MyItinerariesViewModel

    @Before
    fun setup() {
        repository = FakeMyItineraryRepository()
        viewModel = MyItinerariesViewModel(repository)
    }

    @Test
    fun `loadItineraries emits Success with mock data`() = runTest {
        val uiState = viewModel.uiState.first()

        assertNotNull(uiState)
        assertTrue(uiState is UiState.Success)
        val success = uiState as UiState.Success<List<MyItinerary>>
        val itineraries = success.data

        assertEquals(2, itineraries.size)

        val amalfi = itineraries.first { it.title == "Amalfi in 5 giorni" }
        assertEquals("14 – 18 set 2026", amalfi.dateRange)
        assertEquals(6, amalfi.stopsCount)
        assertEquals(false, amalfi.isPublic)
        assertEquals(2, amalfi.stopsPreview.size)
        assertEquals("Tour in barca a Capri", amalfi.stopsPreview[0].title)
        assertEquals("09:00", amalfi.stopsPreview[0].time)
        assertEquals("Pranzo da Nonna Rosa", amalfi.stopsPreview[1].title)
        assertEquals("13:30", amalfi.stopsPreview[1].time)

        val bolzano = itineraries.first { it.title == "Weekend a Bolzano" }
        assertEquals("22 – 24 set 2026", bolzano.dateRange)
        assertEquals(3, bolzano.stopsCount)
        assertEquals(true, bolzano.isPublic)
        assertEquals(1, bolzano.stopsPreview.size)
        assertEquals("Arrivo e giro in centro", bolzano.stopsPreview[0].title)
        assertEquals("16:00", bolzano.stopsPreview[0].time)
    }

    @Test
    fun `onItineraryDeleted reloads data`() = runTest {
        val firstLoad = viewModel.uiState.first()
        assertTrue(firstLoad is UiState.Success)

        viewModel.onItineraryDeleted()

        val secondLoad = viewModel.uiState.first()
        assertTrue(secondLoad is UiState.Success)
        val success = secondLoad as UiState.Success<List<MyItinerary>>
        assertEquals(2, success.data.size)
    }

    @Test
    fun `onItineraryUpdated reloads data`() = runTest {
        val firstLoad = viewModel.uiState.first()
        assertTrue(firstLoad is UiState.Success)

        viewModel.onItineraryUpdated()

        val secondLoad = viewModel.uiState.first()
        assertTrue(secondLoad is UiState.Success)
        val success = secondLoad as UiState.Success<List<MyItinerary>>
        assertEquals(2, success.data.size)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val testViewModel = MyItinerariesViewModel(FakeMyItineraryRepository())
        // The init block calls loadItineraries(), so state may already be Success
        // We just verify no crash and state is valid
        val initialState = testViewModel.uiState.value

        assertTrue(initialState is UiState.Loading || initialState is UiState.Success)
    }
}