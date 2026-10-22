package com.tripflow.feature.itinerary.ui.itinerarydetail

import android.annotation.SuppressLint
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.tripflow.core.model.UiState
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import org.junit.Rule
import org.junit.Test

class ItineraryDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @SuppressLint("ViewModelConstructorInComposable")
    @Test
    fun readOnlyItineraryHidesEditingActions() {
        composeRule.setContent {
            TripFlowTheme {
                ItineraryDetailScreen(
                    itineraryId = "1",
                    viewModel = ItineraryDetailViewModel(ReadOnlyItineraryRepository()),
                )
            }
        }

        composeRule.waitUntil(5_000) {
            composeRule
                .onAllNodesWithText("Costa Amalfitana")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText("Tappa").assertDoesNotExist()
        composeRule.onNodeWithText("Aggiungi tappa").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Elimina tappa").assertDoesNotExist()
    }

    private class ReadOnlyItineraryRepository : ItineraryRepository by FakeItineraryRepository() {
        override suspend fun getItineraryDetail(id: String): UiState<ItineraryDetail> {
            return when (val state = FakeItineraryRepository().getItineraryDetail(id)) {
                is UiState.Success -> UiState.Success(state.data.copy(canEdit = false))
                else -> state
            }
        }
    }
}
