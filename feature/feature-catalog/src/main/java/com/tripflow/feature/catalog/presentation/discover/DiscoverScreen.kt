package com.tripflow.feature.catalog.presentation.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.core.ui.component.InfoChip
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.component.TripCard
import com.tripflow.core.ui.component.TripCardUi
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.format.Formatters
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import java.math.BigDecimal
import java.time.LocalDate


class DiscoverViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DiscoverViewModel(CatalogRepositoryImpl()) as T
    }
}

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = viewModel(factory = DiscoverViewModelFactory()),
    onTripClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val filters by viewModel.filters.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        val tripsCount = (uiState as? UiState.Success<List<TripResponseDTO>>)?.data?.size ?: 0

        DiscoverHeader(
            tripsCount = tripsCount,
            onProfileClick = onProfileClick
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {

            TripFlowTextField(
                value = filters.query,
                onValueChange = viewModel::updateQuery,
                label = "",
                placeholder = "Dove vuoi andare?",
                error = null,
                enabled = true,
                isPassword = false,
                leading = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca"
                    )
                },
                trailing = {},
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { /* TODO: Aprire bottom sheet filtri avanzati */ },
                modifier = Modifier
                    .size(Dimens.fieldHeight)
                    .clip(RoundedCornerShape(Dimens.radiusButton))
                    .background(MaterialTheme.colorScheme.onBackground)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filtri",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.gapM))

        FilterRow(
            filters = filters,
            onToggleAvailable = viewModel::toggleAvailableSpots,
            onTogglePrice = { viewModel.toggleMaxPrice(500.0) },
            onToggleMonth = { viewModel.toggleMonth(9) },
            onClearFilters = {
                if (filters.showOnlyAvailable) viewModel.toggleAvailableSpots()
                if (filters.maxPrice != null) viewModel.toggleMaxPrice(filters.maxPrice!!)
                if (filters.targetMonth != null) viewModel.toggleMonth(filters.targetMonth!!)
            }
        )

        Spacer(modifier = Modifier.height(Dimens.gapM))

        StateHost<List<TripResponseDTO>>(
            state = uiState,
            onRetry = viewModel::loadTrips,
            modifier = Modifier.weight(1f),
            emptyTitle = "Nessun viaggio trovato",
            emptyMessage = "Prova a modificare i filtri di ricerca.",
            emptyActionLabel = "Riprova",
            onEmptyAction = viewModel::loadTrips
        ) { trips ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.screenPadding,
                    end = Dimens.screenPadding,
                    bottom = Dimens.screenPadding
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapL)
            ) {
                items(
                    items = trips,
                    key = { it.id }
                ) { trip ->
                    TripCard(
                        trip = trip.toTripCardUi(),
                        onClick = { onTripClick(trip.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoverHeader(
    tripsCount: Int,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.screenPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Scopri",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "$tripsCount viaggi in partenza",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        UserBadge(
            name = "Giulio Rossi",
            caption = null,
            imageUrl = null,
            modifier = Modifier.clickable { onProfileClick() }
        )
    }
}

@Composable
private fun FilterRow(
    filters: DiscoverFilters,
    onToggleAvailable: () -> Unit,
    onTogglePrice: () -> Unit,
    onToggleMonth: () -> Unit,
    onClearFilters: () -> Unit
) {
    val isAllSelected = !filters.showOnlyAvailable && filters.maxPrice == null && filters.targetMonth == null

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapS),
        contentPadding = PaddingValues(horizontal = Dimens.screenPadding)
    ) {
        item {
            InfoChip(
                text = "Tutti",
                highlighted = isAllSelected,
                leading = {},
                modifier = Modifier.clickable { onClearFilters() }
            )
        }
        item {
            InfoChip(
                text = "Posti liberi",
                highlighted = filters.showOnlyAvailable,
                leading = {},
                modifier = Modifier.clickable { onToggleAvailable() }
            )
        }
        item {
            InfoChip(
                text = "Sotto € 500",
                highlighted = filters.maxPrice == 500.0,
                leading = {},
                modifier = Modifier.clickable { onTogglePrice() }
            )
        }
        item {
            InfoChip(
                text = "Settembre",
                highlighted = filters.targetMonth == 9,
                leading = {},
                modifier = Modifier.clickable { onToggleMonth() }
            )
        }
    }
}

private fun TripResponseDTO.toTripCardUi(): TripCardUi {
    val start = runCatching { LocalDate.parse(this.startDate) }.getOrNull()
    val end = runCatching { LocalDate.parse(this.endDate) }.getOrNull()

    val dateString = if (start != null && end != null) {
        Formatters.dateRange(start, end)
    } else {
        "Date non disponibili"
    }

    val priceString = Formatters.money(BigDecimal.valueOf(this.price))
    val coverImage = this.images?.firstOrNull()

    return TripCardUi(
        id = this.id,
        title = this.name,
        destination = this.destination,
        dateRange = dateString,
        priceLabel = priceString,
        imageUrl = coverImage,
        rating = this.rating,
        spotsLeft = this.availableSpots
    )
}