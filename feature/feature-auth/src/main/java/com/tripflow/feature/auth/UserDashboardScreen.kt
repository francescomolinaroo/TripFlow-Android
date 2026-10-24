package com.tripflow.feature.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripflow.core.model.UiState
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.component.DangerButton
import com.tripflow.core.ui.component.Status
import com.tripflow.core.ui.component.StatusChip
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.LocalSessionKey
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@Composable
fun UserDashboardScreen(
    viewModel: UserDashboardViewModel = hiltViewModel(key = "dashboard-${LocalSessionKey.current}"),
    onBackClick: (() -> Unit)? = null,
    onLogoutClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {},
    onEditProfileClick: (UserResponse) -> Unit = {},
    onBookingsClick: () -> Unit = {},
    onReviewsClick: () -> Unit = {},
    onMyTripsClick: () -> Unit = {},
    onReceivedBookingsClick: () -> Unit = {},
    onCreateTripClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            viewModel.onNavigated()
            onLogoutClick()
        }
    }

    LaunchedEffect(state.sessionExpired) {
        if (state.sessionExpired) {
            viewModel.onNavigated()
            onSessionExpired()
        }
    }

    Scaffold(containerColor = TripFlowColors.Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapL)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.gapS)) {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = TripFlowColors.TextPrimary)
                    }
                }
                Text("Il mio profilo", style = MaterialTheme.typography.headlineMedium, color = TripFlowColors.TextPrimary)
            }

            when (val profileState = state.profileState) {
                UiState.Loading -> Text("Caricamento profilo...", color = TripFlowColors.TextSecondary)
                is UiState.Success -> {
                    val role = dashboardRole(profileState.data.role)

                    Text("Benvenuto ${profileState.data.firstName}", style = MaterialTheme.typography.headlineSmall, color = TripFlowColors.TextPrimary)
                    UserBadge(
                        name = "${profileState.data.firstName} ${profileState.data.lastName}",
                        caption = profileState.data.email,
                        imageUrl = profileState.data.profileImage,
                        size = 64.dp
                    )
                    StatusChip(style = Status.ruolo(profileState.data.role))

                    Text(
                        text = when (role) {
                            DashboardRole.ORGANIZER -> "AREA ORGANIZZATORE"
                            DashboardRole.TRAVELER -> "AREA VIAGGIATORE"
                            DashboardRole.UNKNOWN -> "AREA ACCOUNT"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TripFlowColors.TextSecondary
                    )

                    when (role) {
                        DashboardRole.ORGANIZER -> {
                            ProfileAction(icon = Icons.Default.AddCircle, title = "Crea nuovo viaggio", onClick = onCreateTripClick)
                            ProfileAction(icon = Icons.Default.ListAlt, title = "I miei viaggi e itinerari", onClick = onMyTripsClick)
                            ProfileAction(icon = Icons.Default.CreditCard, title = "Prenotazioni ricevute", onClick = onReceivedBookingsClick)
                        }
                        DashboardRole.TRAVELER -> {
                            ProfileAction(icon = Icons.Default.CreditCard, title = "Le mie prenotazioni", onClick = onBookingsClick)
                            ProfileAction(icon = Icons.Default.Star, title = "Le mie recensioni", onClick = onReviewsClick)
                        }
                        DashboardRole.UNKNOWN -> Unit
                    }

                    Spacer(modifier = Modifier.height(Dimens.gapS))
                    Text("ACCOUNT", style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
                    ProfileAction(icon = Icons.Default.Edit, title = "Modifica profilo", onClick = { onEditProfileClick(profileState.data) })
                    DangerButton(text = "Esci", onClick = viewModel::logout)
                }
                is UiState.Error -> Text(profileState.message, color = TripFlowColors.Error)
                is UiState.Empty -> Text(profileState.message ?: "Profilo non disponibile", color = TripFlowColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun ProfileAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TripFlowColors.Border, RoundedCornerShape(Dimens.radiusRow))
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.gapM, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        Icon(icon, contentDescription = null, tint = TripFlowColors.TextSecondary)
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TripFlowColors.TextSecondary)
    }
}

@Preview(showBackground = true)
@Composable
private fun UserDashboardScreenPreview() {
    TripFlowTheme { }
}
