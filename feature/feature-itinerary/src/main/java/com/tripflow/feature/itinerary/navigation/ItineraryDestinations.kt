package com.tripflow.feature.itinerary.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tripflow.feature.itinerary.ui.addstage.AddStageScreen
import com.tripflow.feature.itinerary.ui.itinerarydetail.ItineraryDetailScreen
import com.tripflow.feature.itinerary.ui.myitineraries.MyItinerariesScreen

object ItineraryDestinations {
    const val ROUTE_LIST = "list"
    const val ROUTE_DETAIL = "detail/{itineraryId}"
    const val ROUTE_ADD_STAGE = "add-stage/{itineraryId}"

    fun detailRoute(itineraryId: String): String = "detail/$itineraryId"

    fun addStageRoute(itineraryId: String): String = "add-stage/$itineraryId"

    fun NavGraphBuilder.itineraryGraph(
        onNavigateToDetail: (String) -> Unit = {},
        onNavigateToAddStage: (String) -> Unit = {},
        onNavigateBack: () -> Unit = {},
    ) {
        composable(route = ROUTE_LIST) {
            MyItinerariesScreen(
                onItineraryClick = { itinerary ->
                    itinerary.id
                        .toString()
                        .takeIf { it.isNotBlank() }
                        ?.let(onNavigateToDetail)
                }
            )
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: ""
            ItineraryDetailScreen(
                itineraryId = itineraryId,
                onBack = onNavigateBack,
                onAddStageClick = { onNavigateToAddStage(itineraryId) }
            )
        }

        composable(
            route = ROUTE_ADD_STAGE,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: ""
            AddStageScreen(
                itineraryId = itineraryId,
                onBack = onNavigateBack,
                onStopAdded = onNavigateBack,
            )
        }
    }
}
