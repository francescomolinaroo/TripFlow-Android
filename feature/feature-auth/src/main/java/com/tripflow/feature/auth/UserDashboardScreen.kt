package com.tripflow.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onLogoutClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onBookingsClick: () -> Unit = {}
) {
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

            UserBadge(
                name = "Mario Rossi",
                caption = "Profilo personale",
                size = 64.dp
            )
            StatusChip(style = Status.ruolo("TRAVELER"))

            Spacer(modifier = Modifier.height(Dimens.gapS))

            SecondaryButton(
                text = "Modifica profilo",
                onClick = onEditProfileClick
            )
            SecondaryButton(
                text = "Le mie prenotazioni",
                onClick = onBookingsClick
            )
            DangerButton(
                text = "Esci",
                onClick = onLogoutClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserDashboardScreenPreview() {
    TripFlowTheme {
        UserDashboardScreen()
    }
}
