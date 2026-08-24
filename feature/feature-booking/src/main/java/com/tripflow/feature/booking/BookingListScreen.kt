package com.tripflow.feature.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tripflow.core.ui.component.Status
import com.tripflow.core.ui.component.StatusChip
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@Composable
fun BookingListScreen(
    onBookingClick: (String) -> Unit = {},
    onWriteReviewClick: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
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
                    selectedTabIndex = selectedTab,
                    containerColor = TripFlowColors.Background,
                    contentColor = TripFlowColors.Accent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = TripFlowColors.Accent
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    style = if (selectedTab == index)
                                        MaterialTheme.typography.titleSmall
                                    else
                                        MaterialTheme.typography.bodyMedium,
                                    color = if (selectedTab == index) TripFlowColors.TextPrimary else TripFlowColors.TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        val bookings = remember { //mock per ora
            listOf(
                BookingUi(
                    id = "1",
                    title = "Costa Amalfitana",
                    date = "14 – 18 set 2026",
                    location = "Amalfi",
                    participants = 2,
                    price = 1050,
                    status = "IN_ATTESA",
                    action = "Completa il pagamento",
                    actionIcon = Icons.Default.CreditCard
                ),
                BookingUi(
                    id = "2",
                    title = "Trekking in Val di Funes",
                    date = "22 – 27 set 2026",
                    location = "Bolzano",
                    participants = 1,
                    price = 620,
                    status = "CONFERMATA",
                    action = null
                ),
                BookingUi(
                    id = "3",
                    title = "Isole Eolie in barca",
                    date = "2 – 7 giu 2026",
                    location = "Lipari",
                    participants = 2,
                    price = 890,
                    status = "COMPLETATA",
                    action = "Scrivi una recensione",
                    actionIcon = Icons.Default.Star
                )
            )
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapXL)
        ) {
            items(bookings) { booking ->
                BookingCard(
                    booking = booking,
                    onClick = { onBookingClick(booking.id) },
                    onActionClick = { onWriteReviewClick(booking.id)}
                )
            }
        }
    }
}

data class BookingUi(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val participants: Int,
    val price: Int,
    val status: String,
    val action: String? = null,
    val actionIcon: ImageVector? = null
)

@Composable
fun BookingCard(
    booking: BookingUi,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TripFlowColors.Surface)
            .clickable(onClick = onClick)
            .padding(Dimens.gapM),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = null,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(Dimens.radiusChip))
                    .background(TripFlowColors.Surface2),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(booking.title, style = MaterialTheme.typography.titleMedium)
                    StatusChip(style = Status.prenotazione(booking.status))
                }
                Text("${booking.date} · ${booking.location}", style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
                Text(
                    "${booking.participants} partecipant${if (booking.participants > 1) "i" else "e"}  ·  € ${booking.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextBody,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (booking.action != null) {
            HorizontalDivider(color = TripFlowColors.Divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onActionClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (booking.actionIcon != null) {
                        Icon(
                            booking.actionIcon,
                            contentDescription = null,
                            tint = if (booking.status == "IN_ATTESA") TripFlowColors.Warning else TripFlowColors.Accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        booking.action,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (booking.status == "IN_ATTESA") TripFlowColors.Warning else TripFlowColors.Accent
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TripFlowColors.TextSecondary)
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
