package com.tripflow.feature.itinerary.ui.itinerarydetail

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.MainDispatcherRule
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ItineraryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadItinerary exposes detail with stages sorted by order`() = runTest {
        val viewModel = ItineraryDetailViewModel(FakeItineraryRepository())

        viewModel.loadItinerary("1")

        val state = viewModel.uiState.value as UiState.Success<ItineraryDetail>
        assertEquals("Costa Amalfitana", state.data.title)
        assertEquals(state.data.stages.size, state.data.totalStages)
        assertEquals(
            state.data.stages.map { it.orderIndex }.sorted(),
            state.data.stages.map { it.orderIndex },
        )
    }

    @Test
    fun `deleteStage removes stage locally and emits confirmation`() = runTest {
        val viewModel = ItineraryDetailViewModel(FakeItineraryRepository())
        viewModel.loadItinerary("1")
        val detail = (viewModel.uiState.value as UiState.Success<ItineraryDetail>).data
        val stageToDelete = detail.stages.first()
        val previousCount = detail.stages.size

        viewModel.deleteStage(stageToDelete.id)

        val updated = (viewModel.uiState.value as UiState.Success<ItineraryDetail>).data
        assertFalse(updated.stages.any { it.id == stageToDelete.id })
        assertEquals(previousCount - 1, updated.stages.size)
        assertEquals(previousCount - 1, updated.totalStages)
        assertEquals("Tappa eliminata", viewModel.actionState.value.message)
        assertNull(viewModel.actionState.value.deletingStageId)
    }

    @Test
    fun `loadItinerary exposes retryable error`() = runTest {
        val repository = object : ItineraryRepository by FakeItineraryRepository() {
            override suspend fun getItineraryDetail(id: String): UiState<ItineraryDetail> =
                UiState.Error("Itinerario non disponibile", retryable = false)
        }
        val viewModel = ItineraryDetailViewModel(repository)

        viewModel.loadItinerary("missing")

        val state = viewModel.uiState.value as UiState.Error
        assertEquals("Itinerario non disponibile", state.message)
        assertFalse(state.retryable)
    }

    @Test
    fun `deleteStage reports repository error without removing stages`() = runTest {
        val repository = object : ItineraryRepository by FakeItineraryRepository() {
            override suspend fun deleteStage(itineraryId: String, stageId: String): UiState<Unit> =
                UiState.Error("Permesso negato")
        }
        val viewModel = ItineraryDetailViewModel(repository)
        viewModel.loadItinerary("1")
        val before = (viewModel.uiState.value as UiState.Success<ItineraryDetail>).data

        viewModel.deleteStage(before.stages.first().id)

        val after = (viewModel.uiState.value as UiState.Success<ItineraryDetail>).data
        assertEquals(before.stages.size, after.stages.size)
        assertEquals("Permesso negato", viewModel.actionState.value.errorMessage)
        assertTrue(viewModel.actionState.value.deletingStageId == null)
    }

    @Test
    fun `retry after load error replaces error with detail`() = runTest {
        val fake = FakeItineraryRepository()
        var shouldFail = true
        val repository = object : ItineraryRepository by fake {
            override suspend fun getItineraryDetail(id: String): UiState<ItineraryDetail> {
                if (shouldFail) {
                    shouldFail = false
                    return UiState.Error("Itinerario non disponibile")
                }
                return fake.getItineraryDetail(id)
            }
        }
        val viewModel = ItineraryDetailViewModel(repository)

        viewModel.loadItinerary("1")
        assertTrue(viewModel.uiState.value is UiState.Error)

        viewModel.loadItinerary("1")

        val state = viewModel.uiState.value as UiState.Success<ItineraryDetail>
        assertEquals("Costa Amalfitana", state.data.title)
    }

    @Test
    fun `clearActionMessage resets action error`() = runTest {
        val repository = object : ItineraryRepository by FakeItineraryRepository() {
            override suspend fun deleteStage(itineraryId: String, stageId: String): UiState<Unit> =
                UiState.Error("Permesso negato")
        }
        val viewModel = ItineraryDetailViewModel(repository)
        viewModel.loadItinerary("1")
        val stageId = (viewModel.uiState.value as UiState.Success<ItineraryDetail>)
            .data.stages.first().id
        viewModel.deleteStage(stageId)

        viewModel.clearActionMessage()

        assertNull(viewModel.actionState.value.errorMessage)
    }
}
