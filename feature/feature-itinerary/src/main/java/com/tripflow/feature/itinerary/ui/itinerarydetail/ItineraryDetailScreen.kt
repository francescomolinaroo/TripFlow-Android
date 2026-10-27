package com.tripflow.feature.itinerary.ui.itinerarydetail

import android.content.Intent
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.graphics.Color
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
    val snackbarHostState = remember { SnackbarHostState() }
    var stageToDelete by remember { mutableStateOf<StageDetail?>(null) }
    var visibilityMenuOpen by remember { mutableStateOf(false) }
    var confirmMakePrivate by remember { mutableStateOf(false) }
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

    val sharePublicLink: (String) -> Unit = { link ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }
        context.startActivity(Intent.createChooser(intent, "Condividi itinerario"))
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
                    if (detail != null && isOwner) {
                        Box {
                            IconButton(
                                onClick = { visibilityMenuOpen = true },
                                enabled = !actionState.isUpdatingVisibility,
                            ) {
                                Icon(
                                    imageVector = detail.visibility.icon(),
                                    contentDescription = "Cambia visibilita'",
                                    tint = TripFlowColors.TextPrimary,
                                )
                            }
                            DropdownMenu(
                                expanded = visibilityMenuOpen,
                                onDismissRequest = { visibilityMenuOpen = false },
                            ) {
                                // SHARED non si sceglie da qui: diventa tale condividendo l'itinerario.
                                listOf(Visibility.PRIVATE, Visibility.PUBLIC).forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label()) },
                                        leadingIcon = { Icon(option.icon(), contentDescription = null) },
                                        trailingIcon = {
                                            if (detail.visibility == option) {
                                                Icon(Icons.Filled.Check, contentDescription = "Attuale")
                                            }
                                        },
                                        onClick = { requestVisibility(option) },
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
                    containerColor = TripFlowColors.TextPrimary,
                    contentColor = Color.White,
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
                    onSharePublicLink = sharePublicLink,
                )
            },
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
    onSharePublicLink: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.screenPadding,
            end = Dimens.screenPadding,
            top = Dimens.gapM,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapM),
    ) {
        item {
            ItineraryHeader(itinerary, onSharePublicLink)
        }

        if (itinerary.stages.isEmpty()) {
            item {
                EmptyStages(canEdit = canEdit, onAddStage = onAddStage)
            }
        } else {
            itemsIndexed(
                items = itinerary.stages,
                key = { _, stage -> stage.id },
            ) { index, stage ->
                StageCard(
                    stage = stage,
                    position = index + 1,
                    canEdit = canEdit,
                    isDeleting = deletingStageId == stage.id,
                    onDelete = { onDeleteStage(stage) },
                )
            }
        }
    }
}

@Composable
private fun ItineraryHeader(
    itinerary: ItineraryDetail,
    onSharePublicLink: (String) -> Unit = {},
) {
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
            val publicToken = itinerary.publicToken
            if (itinerary.visibility == Visibility.PUBLIC && !publicToken.isNullOrBlank()) {
                PublicLinkRow(onShare = { onSharePublicLink(PublicLink.forToken(publicToken)) })
            }
        }
    }
}

@Composable
private fun PublicLinkRow(onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.gapS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Public,
                contentDescription = null,
                tint = TripFlowColors.Accent,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Link pubblico attivo",
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextBody,
                modifier = Modifier.padding(start = Dimens.gapS),
            )
        }
        TextButton(onClick = onShare) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Condividi", modifier = Modifier.padding(start = 4.dp))
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
private fun StageCard(
    stage: StageDetail,
    position: Int,
    canEdit: Boolean,
    isDeleting: Boolean,
    onDelete: () -> Unit,
) {
    Surface(
        color = TripFlowColors.Background,
        shape = RoundedCornerShape(Dimens.radiusCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TripFlowColors.Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.gapL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapM),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                color = TripFlowColors.AccentSoft,
                shape = RoundedCornerShape(Dimens.radiusChip),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = TripFlowColors.Accent,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stage.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TripFlowColors.TextPrimary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gapM),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TripFlowColors.TextSecondary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "${stage.startTime} - ${stage.endTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TripFlowColors.TextSecondary,
                        )
                    }
                    Text(
                        text = Formatters.date(stage.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = TripFlowColors.TextSecondary,
                    )
                }
                if (!stage.location.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TripFlowColors.TextSecondary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = stage.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = TripFlowColors.TextSecondary,
                        )
                    }
                }
                stage.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TripFlowColors.TextBody,
                    )
                }
                StatusPill(stage.stopType.label())
            }

            if (canEdit) {
                IconButton(
                    onClick = onDelete,
                    enabled = !isDeleting,
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = TripFlowColors.Accent,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina tappa",
                            tint = TripFlowColors.TextSecondary,
                        )
                    }
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
