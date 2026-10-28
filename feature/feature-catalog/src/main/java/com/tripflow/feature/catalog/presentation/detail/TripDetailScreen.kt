package com.tripflow.feature.catalog.presentation.detail

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.core.network.review.dto.ReviewResponseDTO
import com.tripflow.core.ui.component.DangerButton
import com.tripflow.core.ui.component.InfoChip
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.format.Formatters
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import com.tripflow.feature.catalog.data.repository.ReviewRepositoryImpl
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class TripDetailViewModelFactory(private val context: Context? = null) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TripDetailViewModel(
            CatalogRepositoryImpl(),
            ReviewRepositoryImpl(),
            AuthRepository(context)
        ) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    viewModel: TripDetailViewModel = viewModel(
        key = "trip_detail_$tripId",
        factory = TripDetailViewModelFactory(LocalContext.current.applicationContext)
    ),
    isCurrentUserOrganizer: Boolean = false,
    onBackClick: () -> Unit,
    onBookClick: (String) -> Unit,
    onAddActivityClick: (String) -> Unit = {},
    onEditTripClick: (String) -> Unit = {},
    onTripDeleted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        viewModel.loadTripDetails(tripId)
    }

    val currentData = (uiState as? UiState.Success<TripDetailData>)?.data
    val isOrganizer = currentData?.let { it.isOrganizer || isCurrentUserOrganizer } ?: isCurrentUserOrganizer

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (isOrganizer) {
                        IconButton(onClick = { onEditTripClick(tripId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifica viaggio",
                                tint = TripFlowColors.TextPrimary
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Elimina viaggio",
                                tint = TripFlowColors.Error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (currentData != null) {
                if (!isOrganizer) {
                    val data = currentData.toUiModel()
                    BottomBookingBar(price = data.priceString, onBookClick = { onBookClick(tripId) })
                } else {
                    OrganizerBottomBar(
                        onEditClick = { onEditTripClick(tripId) },
                        onDeleteClick = { showDeleteDialog = true }
                    )
                }
            }
        }
    ) { paddingValues ->
        StateHost<TripDetailData>(
            state = uiState,
            onRetry = { viewModel.loadTripDetails(tripId) },
            modifier = Modifier.padding(paddingValues)
        ) { data ->
            val uiModel = data.toUiModel()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Dimens.screenPadding)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(TripFlowColors.Surface2)
                    ) {
                        if (!uiModel.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = uiModel.imageUrl,
                                contentDescription = uiModel.title,
                                contentScale = ContentScale.Crop,
                                placeholder = ColorPainter(TripFlowColors.Surface2),
                                error = ColorPainter(TripFlowColors.Surface2),
                                fallback = ColorPainter(TripFlowColors.Surface2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(TripFlowColors.Surface2),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = TripFlowColors.TextDisabled,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    TripHeaderSection(uiModel)
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.gapL))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.screenPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ATTIVITÀ DISPONIBILI",
                            style = MaterialTheme.typography.labelSmall
                        )
                        if (isOrganizer) {
                            TextButton(
                                onClick = { onAddActivityClick(tripId) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Aggiungi attività",
                                    tint = TripFlowColors.Accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Aggiungi",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TripFlowColors.Accent
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.gapM))
                }

                if (uiModel.activities.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.screenPadding),
                            colors = CardDefaults.cardColors(containerColor = TripFlowColors.Surface),
                            shape = RoundedCornerShape(Dimens.radiusCard)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.gapL),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Nessuna attività programmata per questo viaggio.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TripFlowColors.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                if (isOrganizer) {
                                    Spacer(modifier = Modifier.height(Dimens.gapM))
                                    PrimaryButton(
                                        text = "+ Aggiungi la prima attività",
                                        onClick = { onAddActivityClick(tripId) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(uiModel.activities) { activity ->
                        ActivityRow(activity)
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.gapL))
                    ReviewSectionHeader(uiModel)
                }

                items(uiModel.reviews) { review ->
                    ReviewRow(review)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = {
                Text(
                    text = "Eliminare questo viaggio?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TripFlowColors.TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                    Text(
                        text = "Sei sicuro di voler eliminare definitivamente questo viaggio? L'operazione non può essere annullata.",
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
                        isDeleting = true
                        deleteError = null
                        viewModel.deleteTrip(
                            tripId = tripId,
                            onDeleted = {
                                isDeleting = false
                                showDeleteDialog = false
                                onTripDeleted()
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
                    onClick = { showDeleteDialog = false },
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
private fun OrganizerBottomBar(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.screenPadding)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecondaryButton(
                text = "Modifica viaggio",
                onClick = onEditClick,
                modifier = Modifier.weight(1f)
            )
            DangerButton(
                text = "Elimina",
                onClick = onDeleteClick,
                modifier = Modifier.weight(0.7f)
            )
        }
    }
}

@Composable
private fun TripHeaderSection(uiModel: TripDetailUiModel) {
    Column(modifier = Modifier.padding(Dimens.screenPadding)) {
        Text(text = uiModel.title, style = MaterialTheme.typography.headlineMedium)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = Dimens.gapS)
        ) {
            Text(text = uiModel.destination, style = MaterialTheme.typography.bodyMedium)
            if (uiModel.reviews.isNotEmpty()) {
                Spacer(modifier = Modifier.width(Dimens.gapM))
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = uiModel.ratingString, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            } else {
                Spacer(modifier = Modifier.width(Dimens.gapM))
                Text(text = uiModel.ratingString, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapS),
            modifier = Modifier.padding(top = Dimens.gapM)
        ) {
            InfoChip(text = uiModel.dateString, leading = {})
            InfoChip(text = uiModel.durationString, leading = {})
            InfoChip(text = uiModel.spotsString, leading = {})
        }

        Spacer(modifier = Modifier.height(Dimens.gapL))

        UserBadge(
            name = uiModel.organizerName,
            caption = "ORGANIZZATO DA",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimens.gapM))

        Text(text = uiModel.description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActivityRow(activity: ActivityUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenPadding, vertical = Dimens.gapS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = activity.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "${activity.duration}m • ${activity.spots} posti",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(text = activity.price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReviewSectionHeader(uiModel: TripDetailUiModel) {
    Column(modifier = Modifier.padding(horizontal = Dimens.screenPadding)) {
        Text(text = "RECENSIONI", style = MaterialTheme.typography.labelSmall)
        if (uiModel.reviews.isNotEmpty()) {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = Dimens.gapS, bottom = Dimens.gapM)) {
                Text(text = uiModel.ratingValue, style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.width(Dimens.gapS))
                Text(text = "basata su ${uiModel.reviews.size} recensioni", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 6.dp))
            }
        } else {
            Text(
                text = "Nessuna recensione disponibile per questo viaggio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.gapS, bottom = Dimens.gapM)
            )
        }
    }
}

@Composable
private fun ReviewRow(review: ReviewUiModel) {
    Column(modifier = Modifier.padding(horizontal = Dimens.screenPadding, vertical = Dimens.gapM)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = review.reviewerName, style = MaterialTheme.typography.titleSmall)
            Text(text = review.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            repeat(review.rating) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
            }
        }
        Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BottomBookingBar(price: String, onBookClick: () -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.screenPadding)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = price, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "a persona", style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onBookClick,
                shape = RoundedCornerShape(Dimens.radiusButton),
                modifier = Modifier.height(Dimens.fieldHeight)
            ) {
                Text("Prenota ora")
            }
        }
    }
}


private data class TripDetailUiModel(
    val title: String,
    val destination: String,
    val ratingString: String,
    val ratingValue: String,
    val dateString: String,
    val durationString: String,
    val spotsString: String,
    val organizerName: String,
    val description: String,
    val priceString: String,
    val imageUrl: String?,
    val activities: List<ActivityUiModel>,
    val reviews: List<ReviewUiModel>
)

private data class ActivityUiModel(val name: String, val duration: Int, val spots: Int, val price: String)
private data class ReviewUiModel(val reviewerName: String, val date: String, val rating: Int, val comment: String)

private fun TripDetailData.toUiModel(): TripDetailUiModel {
    val t = this.trip
    val start = runCatching { LocalDate.parse(t.startDate) }.getOrNull()
    val end = runCatching { LocalDate.parse(t.endDate) }.getOrNull()
    val days = if (start != null && end != null) ChronoUnit.DAYS.between(start, end) else 0

    val hasReviews = this.reviews.isNotEmpty()
    val avgRating = if (hasReviews)
        this.reviews.map { it.valutazione }.average()
    else 0.0
    val avgRatingStr = "%.1f".format(avgRating)

    val coverImage = t.images
        ?.flatMap { it.split(",") }
        ?.map { it.trim() }
        ?.firstOrNull { it.isNotBlank() }

    return TripDetailUiModel(
        title = t.name,
        destination = t.destination,
        ratingValue = if (hasReviews) avgRatingStr else "-",
        ratingString = if (hasReviews) "$avgRatingStr (${this.reviews.size})" else "Nessuna recensione",
        dateString = if (start != null && end != null) Formatters.dateRange(start, end) else "",
        durationString = "$days giorni",
        spotsString = "${t.availableSpots} posti",
        organizerName = this.organizerName,

        description = t.description ?: "Nessuna descrizione disponibile per questo viaggio.",

        priceString = Formatters.money(BigDecimal.valueOf(t.price)),
        imageUrl = coverImage,
        activities = t.activities?.map { a ->
            ActivityUiModel(a.name, a.duration, a.availableSpots, Formatters.money(BigDecimal.valueOf(a.price)))
        } ?: emptyList(),
        reviews = this.reviews.map { r ->
            ReviewUiModel(
                reviewerName = r.autoreNome ?: "Anonimo",
                date = r.createdAt?.take(10) ?: "",
                rating = r.valutazione,
                comment = r.commento ?: ""
            )
        }
    )
}
