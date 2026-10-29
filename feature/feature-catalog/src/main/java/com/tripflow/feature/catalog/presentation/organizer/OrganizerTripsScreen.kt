package com.tripflow.feature.catalog.presentation.organizer

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.core.ui.component.DangerButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.format.Formatters
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import java.math.BigDecimal
import java.time.LocalDate

class OrganizerTripsViewModelFactory(private val context: Context? = null) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OrganizerTripsViewModel(
            catalogRepository = CatalogRepositoryImpl(),
            authRepository = AuthRepository(context)
        ) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerTripsScreen(
    onBack: () -> Unit,
    onTripClick: (String) -> Unit,
    onEditTripClick: (String) -> Unit,
    onCreateTripClick: () -> Unit,
    viewModel: OrganizerTripsViewModel = viewModel(
        factory = OrganizerTripsViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var tripToDelete by remember { mutableStateOf<TripResponseDTO?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadTrips()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "I miei viaggi",
                        style = MaterialTheme.typography.titleLarge,
                        color = TripFlowColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro",
                            tint = TripFlowColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TripFlowColors.Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTripClick,
                containerColor = TripFlowColors.Accent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crea nuovo viaggio")
            }
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        StateHost<List<TripResponseDTO>>(
            state = uiState,
            onRetry = viewModel::loadTrips,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            emptyTitle = "Nessun viaggio pubblicato",
            emptyMessage = "Non hai ancora pubblicato nessun viaggio. Inizia ora creandone uno!",
            emptyActionLabel = "+ Crea viaggio",
            onEmptyAction = onCreateTripClick
        ) { trips ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.screenPadding,
                    end = Dimens.screenPadding,
                    top = Dimens.gapM,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapL)
            ) {
                items(items = trips, key = { it.id }) { trip ->
                    OrganizerTripItem(
                        trip = trip,
                        onClick = { onTripClick(trip.id) },
                        onEdit = { onEditTripClick(trip.id) },
                        onDelete = { tripToDelete = trip }
                    )
                }
            }
        }
    }

    if (tripToDelete != null) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) tripToDelete = null },
            title = {
                Text(
                    text = "Eliminare \"${tripToDelete?.name}\"?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TripFlowColors.TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                    Text(
                        text = "Sei sicuro di voler eliminare definitivamente questo viaggio? Questa azione è irreversibile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TripFlowColors.TextSecondary
                    )
                    if (deleteError != null) {
                        Text(
                            text = deleteError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TripFlowColors.Error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val tId = tripToDelete?.id ?: return@TextButton
                        isDeleting = true
                        deleteError = null
                        viewModel.deleteTrip(
                            tripId = tId,
                            onDeleted = {
                                isDeleting = false
                                tripToDelete = null
                            },
                            onError = { err ->
                                isDeleting = false
                                deleteError = err
                            }
                        )
                    },
                    enabled = !isDeleting
                ) {
                    Text(
                        text = if (isDeleting) "Eliminazione..." else "Elimina",
                        color = TripFlowColors.Error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { tripToDelete = null },
                    enabled = !isDeleting
                ) {
                    Text("Annulla")
                }
            },
            containerColor = TripFlowColors.Background,
            tonalElevation = 0.dp
        )
    }
}

@Composable
private fun OrganizerTripItem(
    trip: TripResponseDTO,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val start = runCatching { LocalDate.parse(trip.startDate) }.getOrNull()
    val end = runCatching { LocalDate.parse(trip.endDate) }.getOrNull()
    val dateString = if (start != null && end != null) Formatters.dateRange(start, end) else "Date non disponibili"
    val priceString = Formatters.money(BigDecimal.valueOf(trip.price))
    val coverImage = trip.images
        ?.flatMap { it.split(",") }
        ?.map { it.trim() }
        ?.firstOrNull { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TripFlowColors.Surface)
            .border(1.dp, TripFlowColors.Border, shape)
            .clickable(onClick = onClick)
    ) {
        if (!coverImage.isNullOrBlank()) {
            AsyncImage(
                model = coverImage,
                contentDescription = trip.name,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(TripFlowColors.Surface2),
                error = ColorPainter(TripFlowColors.Surface2),
                fallback = ColorPainter(TripFlowColors.Surface2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.gapM),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapS)
        ) {
            Text(
                text = trip.name,
                style = MaterialTheme.typography.titleMedium,
                color = TripFlowColors.TextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TripFlowColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$priceString • ${trip.availableSpots} posti",
                    style = MaterialTheme.typography.labelMedium,
                    color = TripFlowColors.AccentDark,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifica",
                            tint = TripFlowColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina",
                            tint = TripFlowColors.Error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

