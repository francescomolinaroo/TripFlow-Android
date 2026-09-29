package com.tripflow.feature.itinerary.navigation

import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.ui.itinerarylist.ItineraryListScreen
import com.tripflow.feature.itinerary.ui.itinerarydetail.ItineraryDetailScreen
import com.tripflow.feature.itinerary.ui.addstage.AddStageScreen

object ItineraryDestinations {
    const val ROUTE_ROOT = "itinerary"
    const val ROUTE_LIST = "list"
    const val ROUTE_DETAIL = "detail/{itineraryId}"
    const val ROUTE_ADD_STAGE = "add-stage/{itineraryId}"

    // TODO: Riabilitare quando navigation-compose è compatibile con Kotlin 2.x
    // fun NavGraphBuilder.itineraryNavGraph(
    //     onNavigateToDetail: (ItinerarySummary) -> Unit = {},
    //     onNavigateToAddStage: (String) -> Unit = {}
    // ) {
    //     composable(route = ROUTE_LIST) {
    //         ItineraryListScreen(
    //             onItineraryClick = onNavigateToDetail,
    //             onCreateNewClick = { /* TODO: navigate to create itinerary */ }
    //         )
    //     }
    //
    //     composable(
    //         route = ROUTE_DETAIL,
    //         arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
    //     ) { backStackEntry ->
    //         val itineraryId = backStackEntry.getString() ?: ""
    //         ItineraryDetailScreen(
    //             itineraryId = itineraryId,
    //             onAddStageClick = { onNavigateToAddStage(itineraryId) }
    //         )
    //     }
    //
    //     composable(
    //         route = ROUTE_ADD_STAGE,
    //         arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
    //     ) { backStackEntry ->
    //         val itineraryId = backStackEntry.getString() ?: ""
    //         AddStageScreen(itineraryId = itineraryId)
    //     }
    // }
}