package com.tripflow.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.model.UiState
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.component.DangerButton
import com.tripflow.core.ui.component.SecondaryButton
import com.tripflow.core.ui.component.Status
import com.tripflow.core.ui.component.StatusChip
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UserDashboardScreen(
    authRepository: AuthRepository = AuthRepository(),
    onLogoutClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {},
    onEditProfileClick: (UserResponse) -> Unit = {},
    onBookingsClick: () -> Unit = {},
    onReviewsClick: () -> Unit = {}
) {
    var profileState by remember { mutableStateOf<UiState<UserResponse>>(UiState.Loading) }

    LaunchedEffect(Unit) {
        val result = authRepository.getMe()
        profileState = result.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = {
                onSessionExpired()
                UiState.Error(it.message ?: "Profilo non disponibile")
            }
        )
    }

    Scaffold(containerColor = TripFlowColors.Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapL)
        ) {
            Text(
                text = "Il mio profilo",
                style = MaterialTheme.typography.headlineMedium,
                color = TripFlowColors.TextPrimary
            )

            when (val state = profileState) {
                UiState.Loading -> Text("Caricamento profilo...", color = TripFlowColors.TextSecondary)
                is UiState.Success -> {
                    val role = dashboardRole(state.data.role)
                    Text(
                        text = "Benvenuto ${state.data.firstName}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TripFlowColors.TextPrimary
                    )
                    UserBadge(
                        name = "${state.data.firstName} ${state.data.lastName}",
                        caption = state.data.email,
                        imageUrl = state.data.profileImage,
                        size = 64.dp
                    )
                    StatusChip(style = Status.ruolo(state.data.role))

                    Text(
                        text = when (role) {
                            DashboardRole.ORGANIZER -> "AREA ORGANIZZATORE"
                            DashboardRole.TRAVELER -> "AREA VIAGGIATORE"
                            DashboardRole.UNKNOWN -> "AREA ACCOUNT"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TripFlowColors.TextSecondary
                    )
                    if (role == DashboardRole.ORGANIZER) {
                        ProfileAction(icon = Icons.Default.CreditCard, title = "I miei viaggi (demo)", onClick = {})
                        ProfileAction(icon = Icons.Default.Edit, title = "Gestisci attività (demo)", onClick = {})
                        ProfileAction(icon = Icons.Default.Star, title = "Prenotazioni ricevute (demo)", onClick = {})
                    }
                }
                is UiState.Error -> Text(state.message, color = TripFlowColors.Error)
                is UiState.Empty -> Text(state.message ?: "Profilo non disponibile", color = TripFlowColors.TextSecondary)
            }

            Spacer(modifier = Modifier.height(Dimens.gapS))
            Text("ACCOUNT", style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
            ProfileAction(icon = Icons.Default.Edit, title = "Modifica profilo", onClick = {
                val state = profileState
                if (state is UiState.Success) {
                    onEditProfileClick(state.data)
                }
            })
            ProfileAction(icon = Icons.Default.CreditCard, title = "Le mie prenotazioni", onClick = onBookingsClick)
            ProfileAction(icon = Icons.Default.Star, title = "Le mie recensioni", onClick = onReviewsClick)
            DangerButton(
                text = "Esci",
                onClick = {
                    authRepository.logout()
                    onLogoutClick()
                }
            )
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
    TripFlowTheme {
        UserDashboardScreen()
    }
}
