package com.tripflow.feature.catalog.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.core.network.review.dto.ReviewResponseDTO
import com.tripflow.core.ui.component.InfoChip
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.format.Formatters
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import com.tripflow.feature.catalog.data.repository.ReviewRepositoryImpl
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class TripDetailViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TripDetailViewModel(CatalogRepositoryImpl(), ReviewRepositoryImpl()) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    viewModel: TripDetailViewModel = viewModel(factory = TripDetailViewModelFactory()),
    onBackClick: () -> Unit,
    onBookClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tripId) {
        viewModel.loadTripDetails(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (uiState is UiState.Success) {
                val data = (uiState as UiState.Success<TripDetailData>).data.toUiModel()
                BottomBookingBar(price = data.priceString, onBookClick = { onBookClick(tripId) })
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                item {
                    TripHeaderSection(uiModel)
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.gapL))
                    Text(
                        text = "ATTIVITÀ DISPONIBILI",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding)
                    )
                    Spacer(modifier = Modifier.height(Dimens.gapM))
                }

                items(uiModel.activities) { activity ->
                    ActivityRow(activity)
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
            Spacer(modifier = Modifier.width(Dimens.gapM))
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = uiModel.ratingString, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = Dimens.gapS, bottom = Dimens.gapM)) {
            Text(text = uiModel.ratingValue, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.width(Dimens.gapS))
            Text(text = "basata su ${uiModel.reviews.size} recensioni", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 6.dp))
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

    return TripDetailUiModel(
        title = t.name,
        destination = t.destination,
        ratingValue = "4.8", // TODO: Calcolare media dalle recensioni
        ratingString = "4.8 (${this.reviews.size})",
        dateString = if (start != null && end != null) Formatters.dateRange(start, end) else "",
        durationString = "$days giorni",
        spotsString = "${t.availableSpots} posti",
        organizerName = "Organizzatore ${t.organizerId.take(4)}",

        description = t.description ?: "Nessuna descrizione disponibile per questo viaggio.",

        priceString = Formatters.money(BigDecimal.valueOf(t.price)),
        activities = t.activities?.map { a ->
            ActivityUiModel(a.name, a.duration, a.availableSpots, Formatters.money(BigDecimal.valueOf(a.price)))
        } ?: emptyList(),
        reviews = this.reviews.map { r ->
            ReviewUiModel(r.reviewerName, r.createdAt.take(10), r.rating, r.comment)
        }
    )
}