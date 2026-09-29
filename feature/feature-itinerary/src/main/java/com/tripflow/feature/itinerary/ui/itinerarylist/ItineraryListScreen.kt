package com.tripflow.feature.itinerary.ui.itinerarylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.ui.viewmodels.ItineraryListState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryListScreen(
    viewModel: ItineraryListViewModel = viewModel(),
    onItineraryClick: (ItinerarySummary) -> Unit = {},
    onCreateNewClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState: ItineraryListState by viewModel.uiState.collectAsState()

    // Mostra snackbar per errori
    LaunchedEffect(uiState) {
        if (uiState.errorMessage != null) {
            val result = snackbarHostState.showSnackbar(
                message = "${uiState.errorMessage} - Tocca per riprovare",
                actionLabel = "Riprova",
                duration = androidx.compose.material3.SnackbarDuration.Indefinite
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.fetchItineraries()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "I miei itinerari",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        color = TripFlowColors.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TripFlowColors.Background,
                    titleContentColor = TripFlowColors.TextPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.fetchItineraries() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Aggiorna",
                            tint = TripFlowColors.TextPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNewClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Nuovo itinerario") },
                text = { Text("Nuovo itinerario", fontWeight = FontWeight.Medium) },
                modifier = Modifier.padding(Dimens.screenPadding),
                containerColor = TripFlowColors.Accent,
                contentColor = Color.White
            )
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState()
            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.fetchItineraries() }
            )
            uiState.itineraries.isEmpty() -> EmptyState(onCreateNewClick = onCreateNewClick)
            else -> SuccessState(uiState.itineraries, onItineraryClick)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = TripFlowColors.Accent,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    val userFriendlyMessage = when {
        message.contains("401") || message.contains("Sessione scaduta") || message.contains("effettua di nuovo l'accesso") -> 
            "Sessione scaduta. Devi effettuare di nuovo l'accesso."
        message.contains("404") || message.contains("Risorsa non trovata") || message.contains("Nessun itinerario trovato") ->
            "Nessun itinerario trovato."
        message.contains("403") || message.contains("Non hai i permessi") ->
            "Non hai i permessi per visualizzare gli itinerari."
        message.contains("500") || message.contains("Errore del server") || message.contains("Errore interno") ->
            "Errore del server. Riprova più tardi."
        else -> message
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            Text(
                text = userFriendlyMessage,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = TripFlowColors.TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            PrimaryButton(
                text = "Riprova",
                onClick = onRetry
            )
        }
    }
}

@Composable
private fun EmptyState(onCreateNewClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            Text(
                text = "Nessun itinerario",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = TripFlowColors.TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Crea il tuo primo itinerario per iniziare",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            PrimaryButton(
                text = "Crea itinerario",
                onClick = onCreateNewClick
            )
        }
    }
}

@Composable
private fun SuccessState(
    itineraries: List<ItinerarySummary>,
    onItemClick: (ItinerarySummary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = Dimens.screenPadding,
            vertical = Dimens.gapM
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        items(itineraries) { itinerary ->
            ItineraryCard(
                itinerary = itinerary,
                onClick = { onItemClick(itinerary) }
            )
        }
    }
}

@Composable
fun ItineraryListScreenPreview() {
    TripFlowTheme {
        val fakeRepo = com.tripflow.feature.itinerary.repository.FakeItineraryRepository()
        val viewModel = ItineraryListViewModel(fakeRepo)

        ItineraryListScreen(
            viewModel = viewModel,
            onItineraryClick = { println("Clicked: ${it.title}") },
            onCreateNewClick = { println("Create new") }
        )
    }
}