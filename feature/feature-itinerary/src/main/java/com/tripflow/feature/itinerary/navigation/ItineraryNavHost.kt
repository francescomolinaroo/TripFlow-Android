package com.tripflow.feature.itinerary.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tripflow.feature.itinerary.navigation.ItineraryDestinations.itineraryGraph

// Contenitore di navigazione della tab "Itinerari": MainActivity lo mostra al posto
// della sola lista, e tutto il flusso (lista, dettaglio, tappe, creazione) vive qui.
// Il back di sistema chiude prima le schermate interne; quando si e' sulla lista
// passa al BackHandler di MainActivity.
@Composable
fun ItineraryNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = ItineraryDestinations.ROUTE_LIST,
        modifier = modifier,
    ) {
        itineraryGraph(
            onNavigateToDetail = { itineraryId ->
                navController.navigate(ItineraryDestinations.detailRoute(itineraryId))
            },
            onNavigateToAddStage = { itineraryId ->
                navController.navigate(ItineraryDestinations.addStageRoute(itineraryId))
            },
            onNavigateToCreate = {
                navController.navigate(ItineraryDestinations.ROUTE_CREATE) {
                    launchSingleTop = true
                }
            },
            // Dopo la creazione si torna alla lista, che si ricarica al resume.
            onItineraryCreated = {
                navController.popBackStack(ItineraryDestinations.ROUTE_LIST, inclusive = false)
            },
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
