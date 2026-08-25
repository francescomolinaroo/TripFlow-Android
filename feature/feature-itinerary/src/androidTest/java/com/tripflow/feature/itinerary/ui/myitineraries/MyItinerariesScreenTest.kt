package com.tripflow.feature.itinerary.ui.myitineraries

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.itinerary.repository.FakeMyItineraryRepository
import org.junit.Rule
import org.junit.Test

class MyItinerariesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun screenRendersTitleAndItineraries() {
        composeRule.setContent {
            TripFlowTheme {
                val fakeRepo = FakeMyItineraryRepository()
                val viewModel = MyItinerariesViewModel(fakeRepo)

                MyItinerariesScreen(viewModel = viewModel)
            }
        }

        composeRule
            .onNodeWithText("I miei itinerari")
            .assertExists()

        composeRule
            .onNodeWithText("Organizza le giornate dei tuoi viaggi, tappa per tappa.")
            .assertExists()

        composeRule
            .onNodeWithText("Amalfi in 5 giorni")
            .assertExists()

        composeRule
            .onNodeWithText("Weekend a Bolzano")
            .assertExists()

        composeRule
            .onNodeWithText("PRIVATO")
            .assertExists()

        composeRule
            .onNodeWithText("PUBBLICO")
            .assertExists()
    }

    @Test
    fun screenShowsFABWithCorrectText() {
        composeRule.setContent {
            TripFlowTheme {
                val fakeRepo = FakeMyItineraryRepository()
                val viewModel = MyItinerariesViewModel(fakeRepo)

                MyItinerariesScreen(viewModel = viewModel)
            }
        }

        composeRule
            .onNodeWithText("+ Nuovo itinerario", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun screenShowsDateRangeAndStopsCount() {
        composeRule.setContent {
            TripFlowTheme {
                val fakeRepo = FakeMyItineraryRepository()
                val viewModel = MyItinerariesViewModel(fakeRepo)

                MyItinerariesScreen(viewModel = viewModel)
            }
        }

        composeRule
            .onNodeWithText("14 – 18 set 2026 · 6 tappe")
            .assertExists()

        composeRule
            .onNodeWithText("22 – 24 set 2026 · 3 tappe")
            .assertExists()
    }

    @Test
    fun screenShowsStopsPreview() {
        composeRule.setContent {
            TripFlowTheme {
                val fakeRepo = FakeMyItineraryRepository()
                val viewModel = MyItinerariesViewModel(fakeRepo)

                MyItinerariesScreen(viewModel = viewModel)
            }
        }

        composeRule
            .onNodeWithText("Tour in barca a Capri")
            .assertExists()

        composeRule
            .onNodeWithText("Pranzo da Nonna Rosa")
            .assertExists()

        composeRule
            .onNodeWithText("Arrivo e giro in centro")
            .assertExists()

        composeRule
            .onNodeWithText("09:00")
            .assertExists()

        composeRule
            .onNodeWithText("13:30")
            .assertExists()

        composeRule
            .onNodeWithText("16:00")
            .assertExists()
    }

    @Test
    fun screenShowsRemainingStopsIndicator() {
        composeRule.setContent {
            TripFlowTheme {
                val fakeRepo = FakeMyItineraryRepository()
                val viewModel = MyItinerariesViewModel(fakeRepo)

                MyItinerariesScreen(viewModel = viewModel)
            }
        }

        composeRule
            .onNodeWithText("• e altre 4 tappe")
            .assertExists()

        composeRule
            .onNodeWithText("• e altre 2 tappe")
            .assertExists()
    }
}