package com.tripflow.feature.booking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun BookingBottomBar(totalPrice: Int, onPaymentClick: () -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        color = TripFlowColors.Background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenPadding, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapL)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Totale", style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
                Text("€ $totalPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            PrimaryButton(
                text = "Vai al pagamento",
                onClick = onPaymentClick,
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}