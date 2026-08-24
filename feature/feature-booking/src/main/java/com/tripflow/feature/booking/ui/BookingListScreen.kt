package com.tripflow.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripflow.core.ui.component.Status
import com.tripflow.core.ui.component.StatusChip
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.booking.ui.components.BookingCard

@Composable
fun BookingListScreen(
    onBookingClick: (String) -> Unit = {},
    onWriteReviewClick: (String) -> Unit = {},
    viewModel: BookingListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Attive", "Tutte")

    Scaffold(
        topBar = {
            Column(modifier = Modifier
                .background(TripFlowColors.Background)
                .statusBarsPadding()
            ) {
                Text(
                    text = "Le mie prenotazioni",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = Dimens.screenPadding, vertical = 16.dp),
                    fontWeight = FontWeight.Bold
                )
                TabRow(
                    selectedTabIndex = uiState.selectedTabIndex,
                    containerColor = TripFlowColors.Background,
                    contentColor = TripFlowColors.Accent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTabIndex]),
                            color = TripFlowColors.Accent
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTabIndex == index,
                            onClick = { viewModel.onTabSelected(index) },
                            text = {
                                Text(
                                    title,
                                    style = if (uiState.selectedTabIndex == index)
                                        MaterialTheme.typography.titleSmall
                                    else
                                        MaterialTheme.typography.bodyMedium,
                                    color = if (uiState.selectedTabIndex == index) TripFlowColors.TextPrimary else TripFlowColors.TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapXL)
        ) {
            items(uiState.bookings) { booking ->
                BookingCard(
                    booking = booking,
                    onClick = { onBookingClick(booking.id) },
                    onActionClick = { onWriteReviewClick(booking.id)}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingListScreenPreview() {
    TripFlowTheme {
        BookingListScreen()
    }
}
