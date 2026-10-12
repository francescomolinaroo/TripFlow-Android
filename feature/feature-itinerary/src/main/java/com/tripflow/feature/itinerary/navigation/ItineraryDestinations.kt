package com.tripflow.feature.itinerary.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.ui.addstage.AddStageScreen
import com.tripflow.feature.itinerary.ui.itinerarydetail.ItineraryDetailScreen
import com.tripflow.feature.itinerary.ui.itinerarylist.ItineraryListScreen
import com.tripflow.feature.itinerary.ui.itinerarylist.ItineraryListViewModel
import com.tripflow.feature.itinerary.ui.myitineraries.MyItinerariesScreen

object ItineraryDestinations {
    const val ROUTE_ROOT = "itinerary"
    const val ROUTE_LIST = "list"
    const val ROUTE_DETAIL = "detail/{itineraryId}"
    const val ROUTE_ADD_STAGE = "add-stage/{itineraryId}"
    const val ROUTE_MY_ITINERARIES = "my-itineraries"

    fun NavGraphBuilder.itineraryGraph(
        onNavigateToDetail: (String) -> Unit = {},
        onNavigateToAddStage: (String) -> Unit = {}
    ) {
        composable(route = ROUTE_LIST) {
            val viewModel: ItineraryListViewModel = hiltViewModel()
            ItineraryListScreen(
                viewModel = viewModel,
                onItineraryClick = { summary: ItinerarySummary -> onNavigateToDetail(summary.id.toString()) },
                onCreateNewClick = { /* TODO: navigate to create itinerary */ }
            )
        }

        composable(route = ROUTE_MY_ITINERARIES) {
            MyItinerariesScreen(onCreateNewClick = { /* TODO */ })
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.getString() ?: ""
            ItineraryDetailScreen(
                itineraryId = itineraryId,
                onAddStageClick = { onNavigateToAddStage(itineraryId) }
            )
        }

        composable(
            route = ROUTE_ADD_STAGE,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.getString() ?: ""
            AddStageScreen(itineraryId = itineraryId)
        }
    }
}