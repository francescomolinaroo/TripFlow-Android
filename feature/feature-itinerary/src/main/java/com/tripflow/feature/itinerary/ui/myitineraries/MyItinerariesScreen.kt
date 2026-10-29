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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tripflow.core.model.UiState
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.LocalSessionKey
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.itinerary.model.MyItinerary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItinerariesScreen(
    viewModel: MyItinerariesViewModel = hiltViewModel(key = "my-itineraries-${LocalSessionKey.current}"),
    onItineraryClick: (MyItinerary) -> Unit = {},
    onCreateNewClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showMyCode by remember { mutableStateOf(false) }

    // Ricarica la lista quando si torna su questa schermata (es. dopo la creazione
    // di un itinerario o modifiche fatte nel dettaglio).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onScreenResumed()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                actions = {
                    if (viewModel.myUserCode != null) {
                        IconButton(onClick = { showMyCode = true }) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Il mio codice utente",
                                tint = TripFlowColors.TextPrimary,
                            )
                        }
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
                        "Nuovo itinerario",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.padding(Dimens.screenPadding),
                containerColor = TripFlowColors.ButtonContainer,
                contentColor = TripFlowColors.ButtonContent,
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
            empty = { ItinerariesEmptyState(onCreateNewClick) },
            content = { itineraries ->
                SuccessState(itineraries, onItineraryClick)
            }
        )
    }
    val code = viewModel.myUserCode
    if (showMyCode && code != null) {
        MyUserCodeDialog(code = code, onDismiss = { showMyCode = false })
    }
}

@Composable
private fun MyUserCodeDialog(code: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Il tuo codice utente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                Text("Mandalo a chi vuole condividere con te un suo itinerario: dovra' incollarlo in \"Condividi con un utente\".")
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TripFlowColors.TextPrimary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                clipboard.setText(AnnotatedString(code))
                copied = true
            }) {
                Text(if (copied) "Copiato" else "Copia")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Chiudi") }
        },
        containerColor = TripFlowColors.Background,
    )
}

@Composable
private fun ItinerariesEmptyState(onCreateNewClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(TripFlowColors.Surface2, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null,
                tint = TripFlowColors.Accent,
                modifier = Modifier.size(36.dp),
            )
        }
        Text(
            text = "Nessun itinerario",
            style = MaterialTheme.typography.titleLarge,
            color = TripFlowColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.gapXL),
        )
        Text(
            text = "Crea il tuo primo itinerario per organizzare il viaggio giorno per giorno.",
            style = MaterialTheme.typography.bodyMedium,
            color = TripFlowColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.gapS),
        )
        Box(modifier = Modifier.padding(top = Dimens.gapXL)) {
            PrimaryButton(text = "Crea itinerario", onClick = onCreateNewClick)
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
        val fakeRepo = com.tripflow.feature.itinerary.repository.FakeItineraryRepository()
        val viewModel = MyItinerariesViewModel(fakeRepo)

        MyItinerariesScreen(
            viewModel = viewModel,
            onItineraryClick = { println("Clicked: ${it.title}") },
            onCreateNewClick = { println("Create new") }
        )
    }
}