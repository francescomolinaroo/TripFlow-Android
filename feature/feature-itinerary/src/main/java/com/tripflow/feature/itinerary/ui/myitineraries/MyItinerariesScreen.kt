package com.tripflow.feature.itinerary.ui.myitineraries

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripflow.core.model.UiState
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.itinerary.model.MyItinerary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItinerariesScreen(
    viewModel: MyItinerariesViewModel = viewModel(),
    onItineraryClick: (MyItinerary) -> Unit = {},
    onCreateNewClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // Mostra snackbar per errori retryable
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is UiState.Error && currentState.retryable) {
            val result = snackbarHostState.showSnackbar(
                message = "${currentState.message} - Tocca per riprovare",
                actionLabel = "Riprova",
                duration = androidx.compose.material3.SnackbarDuration.Indefinite
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.loadItineraries()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.gapS, bottom = Dimens.gapS),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "I miei itinerari",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TripFlowColors.TextPrimary,
                        )
                        Text(
                            text = "Organizza le giornate dei tuoi viaggi, tappa per tappa.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TripFlowColors.TextSecondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TripFlowColors.Background,
                    titleContentColor = TripFlowColors.TextPrimary
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNewClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Nuovo itinerario") },
                text = {
                    Text(
                        "+ Nuovo itinerario",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.padding(Dimens.screenPadding),
                containerColor = TripFlowColors.TextPrimary,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.radiusButton),
            )
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        StateHost<List<MyItinerary>>(
            state = uiState,
            onRetry = { viewModel.loadItineraries() },
            modifier = Modifier.padding(innerPadding),
            emptyTitle = "Nessun itinerario",
            emptyMessage = "Crea il tuo primo itinerario per iniziare a organizzare i viaggi",
            emptyActionLabel = "Crea itinerario",
            onEmptyAction = onCreateNewClick,
            loading = { LoadingState() },
            content = { itineraries ->
                SuccessState(itineraries, onItineraryClick)
            }
        )
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
private fun SuccessState(
    itineraries: List<MyItinerary>,
    onItemClick: (MyItinerary) -> Unit
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
            MyItineraryCard(
                itinerary = itinerary,
                onClick = { onItemClick(itinerary) }
            )
        }
    }
}

@Composable
fun MyItinerariesScreenPreview() {
    TripFlowTheme {
        val fakeRepo = com.tripflow.feature.itinerary.repository.FakeMyItineraryRepository()
        val viewModel = MyItinerariesViewModel(fakeRepo)

        MyItinerariesScreen(
            viewModel = viewModel,
            onItineraryClick = { println("Clicked: ${it.title}") },
            onCreateNewClick = { println("Create new") }
        )
    }
}