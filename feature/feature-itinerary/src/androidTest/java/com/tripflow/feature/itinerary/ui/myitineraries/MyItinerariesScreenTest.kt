package com.tripflow.feature.itinerary.ui.myitineraries

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.itinerary.repository.FakeItineraryRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyItinerariesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreenContent() {
        composeRule.setContent {
            TripFlowTheme {
                val fakeRepo = FakeItineraryRepository()
                val viewModel = MyItinerariesViewModel(fakeRepo)
                MyItinerariesScreen(viewModel = viewModel)
            }
        }
    }

    private fun waitForItineraries() {
        composeRule.waitForIdle()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Costa Amalfitana").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun screenRendersTitleAndItineraries() {
        setScreenContent()
        waitForItineraries()

        composeRule.onNodeWithText("Costa Amalfitana").assertExists()
        composeRule.onNodeWithText("Trekking Dolomiti").assertExists()
        composeRule.onNodeWithText("Sicilia on the road").assertExists()

        composeRule.onNodeWithText("PRIVATE").assertExists()
        composeRule.onNodeWithText("SHARED").assertExists()
        composeRule.onNodeWithText("PUBLIC").assertExists()
    }

    @Test
    fun screenShowsFABWithCorrectText() {
        setScreenContent()
        waitForItineraries()

        composeRule.onNodeWithText("+ Nuovo itinerario", useUnmergedTree = true).assertExists()
    }

    @Test
    fun screenShowsDateRangeAndStopsCount() {
        setScreenContent()
        waitForItineraries()

        composeRule.onNodeWithText("14 - 18 set 2026 · 12 tappe").assertExists()
        composeRule.onNodeWithText("10 - 15 lug 2026 · 8 tappe").assertExists()
    }

    @Test
    fun screenShowsStopsPreview() {
        setScreenContent()
        waitForItineraries()

        composeRule.onNodeWithText("Arrivo ad Amalfi e check-in").assertExists()
        composeRule.onNodeWithText("Passeggiata sul lungomare").assertExists()
        composeRule.onNodeWithText("Escursione a Positano").assertExists()

        composeRule.onNodeWithText("14:00").assertExists()
        composeRule.onNodeWithText("17:00").assertExists()
        composeRule.onAllNodesWithText("09:00").assertCountEquals(2)
    }

    @Test
    fun screenShowsRemainingStopsIndicator() {
        setScreenContent()
        waitForItineraries()

        composeRule.onNodeWithText("• e altre 9 tappe").assertExists()
        composeRule.onNodeWithText("• e altre 6 tappe").assertExists()
    }
}
