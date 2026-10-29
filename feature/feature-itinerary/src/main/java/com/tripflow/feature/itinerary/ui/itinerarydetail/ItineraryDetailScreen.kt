package com.tripflow.feature.itinerary.ui.itinerarydetail

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tripflow.core.model.UiState
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.format.Formatters
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.StopType
import com.tripflow.feature.itinerary.model.Visibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDetailScreen(
    itineraryId: String,
    viewModel: ItineraryDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddStageClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val shareState by viewModel.shareState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var stageToDelete by remember { mutableStateOf<StageDetail?>(null) }
    var visibilityMenuOpen by remember { mutableStateOf(false) }
    var confirmMakePrivate by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var confirmDeleteItinerary by remember { mutableStateOf(false) }
    var showCalendarSheet by remember { mutableStateOf(false) }
    var isAddingToCalendar by remember { mutableStateOf(false) }
    var pendingCalendarStages by remember { mutableStateOf<List<StageDetail>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(itineraryId) {
        viewModel.loadItinerary(itineraryId)
    }

    DisposableEffect(lifecycleOwner, itineraryId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadItinerary(itineraryId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Itinerario eliminato: si torna alla lista, che si ricarica al resume.
    LaunchedEffect(actionState.itineraryDeleted) {
        if (actionState.itineraryDeleted) onBack()
    }

    LaunchedEffect(actionState.message, actionState.errorMessage) {
        val message = actionState.errorMessage ?: actionState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearActionMessage()
    }

    val detail = (uiState as? UiState.Success)?.data
    val canEdit = detail?.canEdit != false
    val isOwner = detail?.let(viewModel::isOwner) == true

    fun requestVisibility(target: Visibility) {
        visibilityMenuOpen = false
        // Rendere privato un itinerario condiviso revoca tutte le condivisioni: chiediamo conferma.
        if (target == Visibility.PRIVATE && detail?.visibility == Visibility.SHARED) {
            confirmMakePrivate = true
        } else {
            viewModel.updateVisibility(target)
        }
    }

    // --- Calendario ---
    // Singola tappa: apre l'app calendario con l'evento compilato (nessun permesso).
    val addStageToCalendar: (StageDetail) -> Unit = { stage ->
        val title = detail?.title.orEmpty()
        try {
            context.startActivity(CalendarExport.insertIntent(CalendarEvent.from(stage, title)))
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar("Nessuna app calendario installata") }
        }
    }

    fun insertSelected(stages: List<StageDetail>) {
        val title = detail?.title.orEmpty()
        isAddingToCalendar = true
        scope.launch {
            val result = CalendarExport.insertAll(context, stages.map { CalendarEvent.from(it, title) })
            isAddingToCalendar = false
            showCalendarSheet = false
            val message = when (result) {
                is CalendarExport.Result.Added ->
                    if (result.count == 1) "1 evento aggiunto al calendario"
                    else "${result.count} eventi aggiunti al calendario"
                CalendarExport.Result.NoCalendar -> "Nessun calendario disponibile sul telefono"
                is CalendarExport.Result.Failed -> result.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    // Piu' tappe insieme: serve il permesso al calendario, chiesto solo a questo punto.
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            insertSelected(pendingCalendarStages)
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permesso al calendario negato") }
        }
    }

    val addSelectedToCalendar: (List<StageDetail>) -> Unit = { stages ->
        val granted = listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
            .all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (granted) {
            insertSelected(stages)
        } else {
            pendingCalendarStages = stages
            calendarPermissionLauncher.launch(
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
            )
        }
    }

    // Condivide il programma come testo gia' formattato (giorni, orari, luoghi, note),
    // leggibile in qualsiasi app senza installare TripFlow.
    val shareItineraryText: () -> Unit = {
        detail?.let { itinerary ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, itinerary.title)
                putExtra(Intent.EXTRA_TEXT, ItineraryShareText.build(itinerary))
            }
            context.startActivity(Intent.createChooser(intent, "Condividi itinerario"))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dettaglio itinerario",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TripFlowColors.TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = TripFlowColors.TextPrimary,
                        )
                    }
                },
                actions = {
                    if (detail != null && detail.stages.isNotEmpty()) {
                        IconButton(onClick = { showCalendarSheet = true }) {
                            Icon(
                                imageVector = Icons.Outlined.EventAvailable,
                                contentDescription = "Aggiungi al calendario",
                                tint = TripFlowColors.TextPrimary,
                            )
                        }
                    }
                    // Menu "altre opzioni": "Esporta testo" per tutti, le altre voci solo al proprietario.
                    if (detail != null) {
                        Box {
                            IconButton(
                                onClick = { visibilityMenuOpen = true },
                                enabled = !actionState.isUpdatingVisibility,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Altre opzioni",
                                    tint = TripFlowColors.TextPrimary,
                                )
                            }
                            DropdownMenu(
                                expanded = visibilityMenuOpen,
                                onDismissRequest = { visibilityMenuOpen = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Esporta testo") },
                                    leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                                    onClick = {
                                        visibilityMenuOpen = false
                                        shareItineraryText()
                                    },
                                )
                                if (isOwner) {
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Cambia visibilità…") },
                                        leadingIcon = { Icon(detail.visibility.icon(), contentDescription = null) },
                                        onClick = {
                                            visibilityMenuOpen = false
                                            showVisibilityDialog = true
                                        },
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Condividi con un utente…") },
                                        leadingIcon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                                        onClick = {
                                            visibilityMenuOpen = false
                                            viewModel.openShares()
                                        },
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Elimina itinerario", color = TripFlowColors.Error) },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Delete, contentDescription = null, tint = TripFlowColors.Error)
                                        },
                                        onClick = {
                                            visibilityMenuOpen = false
                                            confirmDeleteItinerary = true
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TripFlowColors.Background,
                    titleContentColor = TripFlowColors.TextPrimary,
                ),
            )
        },
        floatingActionButton = {
            if (detail != null && canEdit) {
                ExtendedFloatingActionButton(
                    onClick = onAddStageClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tappa", fontWeight = FontWeight.Medium) },
                    modifier = Modifier.padding(Dimens.screenPadding),
                    containerColor = TripFlowColors.ButtonContainer,
                    contentColor = TripFlowColors.ButtonContent,
                    shape = RoundedCornerShape(Dimens.radiusButton),
                )
            }
        },
        containerColor = TripFlowColors.Background,
    ) { innerPadding ->
        StateHost<ItineraryDetail>(
            state = uiState,
            onRetry = { viewModel.loadItinerary(itineraryId) },
            modifier = Modifier.padding(innerPadding),
            loading = { DetailLoading() },
            content = { itinerary ->
                DetailContent(
                    itinerary = itinerary,
                    deletingStageId = actionState.deletingStageId,
                    canEdit = canEdit,
                    onDeleteStage = { stageToDelete = it },
                    onAddStage = onAddStageClick,
                    onAddToCalendar = addStageToCalendar,
                )
            },
        )
    }

    if (confirmDeleteItinerary && detail != null) {
        AlertDialog(
            onDismissRequest = {
                if (!actionState.isDeletingItinerary) confirmDeleteItinerary = false
            },
            title = { Text("Eliminare l'itinerario?") },
            text = {
                Text("\"${detail.title}\" e tutte le sue tappe verranno eliminati definitivamente. Anche le persone con cui lo hai condiviso non lo vedranno piu'.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDeleteItinerary = false
                        viewModel.deleteItinerary()
                    },
                    enabled = !actionState.isDeletingItinerary,
                ) {
                    Text("Elimina", color = TripFlowColors.Error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDeleteItinerary = false },
                    enabled = !actionState.isDeletingItinerary,
                ) { Text("Annulla") }
            },
            containerColor = TripFlowColors.Background,
            titleContentColor = TripFlowColors.TextPrimary,
            textContentColor = TripFlowColors.TextBody,
        )
    }

    if (showCalendarSheet && detail != null) {
        CalendarSheet(
            stages = detail.stages,
            isAdding = isAddingToCalendar,
            onAdd = addSelectedToCalendar,
            onDismiss = { if (!isAddingToCalendar) showCalendarSheet = false },
        )
    }

    if (shareState.isOpen) {
        ShareDialog(
            state = shareState,
            onShare = viewModel::shareWith,
            onRemove = viewModel::removeShare,
            onDismiss = viewModel::closeShares,
        )
    }

    if (showVisibilityDialog && detail != null) {
        VisibilityDialog(
            current = detail.visibility,
            isSaving = actionState.isUpdatingVisibility,
            onConfirm = { target ->
                showVisibilityDialog = false
                if (target == Visibility.SHARED) {
                    viewModel.switchToShared()
                } else {
                    requestVisibility(target)
                }
            },
            onDismiss = { showVisibilityDialog = false },
        )
    }

    if (confirmMakePrivate) {
        AlertDialog(
            onDismissRequest = { confirmMakePrivate = false },
            title = { Text("Rendere privato l'itinerario?") },
            text = { Text("Tutte le condivisioni verranno revocate e solo tu potrai vederlo.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmMakePrivate = false
                    viewModel.updateVisibility(Visibility.PRIVATE)
                }) {
                    Text("Rendi privato", color = TripFlowColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmMakePrivate = false }) { Text("Annulla") }
            },
            containerColor = TripFlowColors.Background,
            titleContentColor = TripFlowColors.TextPrimary,
            textContentColor = TripFlowColors.TextBody,
        )
    }

    stageToDelete?.let { stage ->
        AlertDialog(
            onDismissRequest = {
                if (actionState.deletingStageId == null) stageToDelete = null
            },
            title = { Text("Eliminare la tappa?") },
            text = {
                Text("\"${stage.title}\" verrà rimossa definitivamente dall'itinerario.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteStage(stage.id)
                        stageToDelete = null
                    },
                    enabled = actionState.deletingStageId == null,
                ) {
                    Text("Elimina", color = TripFlowColors.Error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { stageToDelete = null },
                    enabled = actionState.deletingStageId == null,
                ) {
                    Text("Annulla")
                }
            },
            containerColor = TripFlowColors.Background,
            titleContentColor = TripFlowColors.TextPrimary,
            textContentColor = TripFlowColors.TextBody,
        )
    }
}

@Composable
private fun DetailContent(
    itinerary: ItineraryDetail,
    deletingStageId: String?,
    canEdit: Boolean,
    onDeleteStage: (StageDetail) -> Unit,
    onAddStage: () -> Unit,
    onAddToCalendar: (StageDetail) -> Unit = {},
) {
    val days = remember(itinerary.stages, itinerary.startDate) {
        groupStagesByDay(itinerary.stages, itinerary.startDate)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.screenPadding,
            end = Dimens.screenPadding,
            top = Dimens.gapM,
            bottom = 96.dp,
        ),
    ) {
        item {
            ItineraryHeader(itinerary)
        }

        if (itinerary.stages.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(top = Dimens.gapM)) {
                    EmptyStages(canEdit = canEdit, onAddStage = onAddStage)
                }
            }
        } else {
            // Programma per giorni: intestazione del giorno e timeline delle sue tappe.
            days.forEach { day ->
                item(key = "day-${day.date}") {
                    Box(modifier = Modifier.padding(top = Dimens.gapM)) {
                        DayHeader(day)
                    }
                }
                itemsIndexed(
                    items = day.stages,
                    key = { _, stage -> stage.id },
                ) { index, stage ->
                    TimelineStop(
                        stage = stage,
                        isFirst = index == 0,
                        isLast = index == day.stages.lastIndex,
                        canEdit = canEdit,
                        isDeleting = deletingStageId == stage.id,
                        onDelete = { onDeleteStage(stage) },
                        onAddToCalendar = { onAddToCalendar(stage) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ItineraryHeader(itinerary: ItineraryDetail) {
    Surface(
        color = TripFlowColors.Surface2,
        shape = RoundedCornerShape(Dimens.radiusCard),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.gapL),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapS),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = itinerary.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TripFlowColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                StatusPill(itinerary.visibility.label())
            }
            itinerary.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TripFlowColors.TextBody,
                )
            }
            Text(
                text = Formatters.dateRange(itinerary.startDate, itinerary.endDate),
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary,
            )
            Text(
                text = if (itinerary.totalStages == 1) "1 tappa" else "${itinerary.totalStages} tappe",
                style = MaterialTheme.typography.labelMedium,
                color = TripFlowColors.Accent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private fun Visibility.icon(): ImageVector = when (this) {
    Visibility.PUBLIC -> Icons.Filled.Public
    Visibility.PRIVATE -> Icons.Filled.Lock
    Visibility.SHARED -> Icons.Filled.Group
}

@Composable
private fun EmptyStages(
    canEdit: Boolean,
    onAddStage: () -> Unit,
) {
    Surface(
        color = TripFlowColors.Background,
        shape = RoundedCornerShape(Dimens.radiusCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TripFlowColors.Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.gapXL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.gapS),
        ) {
            Text(
                text = "Nessuna tappa",
                style = MaterialTheme.typography.titleMedium,
                color = TripFlowColors.TextPrimary,
            )
            Text(
                text = if (canEdit) {
                    "Aggiungi la prima tappa per iniziare a costruire il tuo itinerario."
                } else {
                    "Questo itinerario non contiene ancora tappe."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary,
            )
            if (canEdit) {
                TextButton(onClick = onAddStage) {
                    Text("Aggiungi tappa")
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Surface(
        color = TripFlowColors.AccentSoft,
        shape = RoundedCornerShape(Dimens.radiusChip),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TripFlowColors.Accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun DetailLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = TripFlowColors.Accent,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp),
        )
    }
}

private fun StopType.label(): String = when (this) {
    StopType.TRIP -> "Viaggio"
    StopType.ACTIVITY -> "Attività"
    StopType.CUSTOM -> "Personalizzata"
}

private fun Visibility.label(): String = when (this) {
    Visibility.PUBLIC -> "Pubblico"
    Visibility.PRIVATE -> "Privato"
    Visibility.SHARED -> "Condiviso"
}
