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
fun BookingBottomBar(
    totalPrice: Int,
    onPaymentClick: () -> Unit,
    isSubmitting: Boolean = false,
    error: String? = null
) {
    Surface(
        shadowElevation = 8.dp,
        color = TripFlowColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
        ) {
            if (error != null) {
                Text(error, style = MaterialTheme.typography.bodySmall, color = TripFlowColors.Error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.gapL)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Totale", style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
                    Text("€ $totalPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                PrimaryButton(
                    text = if (isSubmitting) "Prenotazione in corso..." else "Vai al pagamento",
                    onClick = onPaymentClick,
                    modifier = Modifier.weight(1.5f),
                    enabled = !isSubmitting
                )
            }
        }
    }
}
