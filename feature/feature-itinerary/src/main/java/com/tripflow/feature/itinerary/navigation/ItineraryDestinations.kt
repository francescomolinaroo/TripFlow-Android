package com.tripflow.feature.itinerary.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tripflow.feature.itinerary.ui.addstage.AddStageScreen
import com.tripflow.feature.itinerary.ui.createitinerary.CreateItineraryScreen
import com.tripflow.feature.itinerary.ui.itinerarydetail.ItineraryDetailScreen
import com.tripflow.feature.itinerary.ui.myitineraries.MyItinerariesScreen

object ItineraryDestinations {
    const val ARG_ITINERARY_ID = "itineraryId"

    const val ROUTE_LIST = "list"
    const val ROUTE_DETAIL = "detail/{$ARG_ITINERARY_ID}"
    const val ROUTE_ADD_STAGE = "add-stage/{$ARG_ITINERARY_ID}"
    const val ROUTE_CREATE = "create"

    fun detailRoute(itineraryId: String): String = "detail/$itineraryId"

    fun addStageRoute(itineraryId: String): String = "add-stage/$itineraryId"

    // Grafo del flusso itinerari: lista -> dettaglio -> aggiunta tappa, lista -> creazione.
    // La navigazione vera e propria (navController) e' delegata al chiamante tramite callback.
    fun NavGraphBuilder.itineraryGraph(
        onNavigateToDetail: (String) -> Unit = {},
        onNavigateToAddStage: (String) -> Unit = {},
        onNavigateToCreate: () -> Unit = {},
        onItineraryCreated: (String) -> Unit = {},
        onNavigateBack: () -> Unit = {},
    ) {
        composable(route = ROUTE_LIST) {
            MyItinerariesScreen(
                onItineraryClick = { itinerary ->
                    itinerary.id
                        .toString()
                        .takeIf { it.isNotBlank() }
                        ?.let(onNavigateToDetail)
                },
                onCreateNewClick = onNavigateToCreate,
            )
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument(ARG_ITINERARY_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString(ARG_ITINERARY_ID) ?: ""
            ItineraryDetailScreen(
                itineraryId = itineraryId,
                onBack = onNavigateBack,
                onAddStageClick = { onNavigateToAddStage(itineraryId) }
            )
        }

        composable(
            route = ROUTE_ADD_STAGE,
            arguments = listOf(navArgument(ARG_ITINERARY_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString(ARG_ITINERARY_ID) ?: ""
            AddStageScreen(
                itineraryId = itineraryId,
                onBack = onNavigateBack,
                onStopAdded = onNavigateBack,
            )
        }

        composable(route = ROUTE_CREATE) {
            CreateItineraryScreen(
                onBack = onNavigateBack,
                onCreated = onItineraryCreated,
            )
        }
    }
}
