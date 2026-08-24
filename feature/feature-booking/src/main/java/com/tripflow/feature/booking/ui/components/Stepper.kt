package com.tripflow.feature.booking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 1,
    maxValue: Int = 99
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapL)
    ) {
        IconButton(
            onClick = { if (value > minValue) onValueChange(value - 1) },
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TripFlowColors.Surface2)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Rimuovi", modifier = Modifier.size(16.dp))
        }
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        IconButton(
            onClick = { if (value < maxValue) onValueChange(value + 1) },
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TripFlowColors.TextPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Aggiungi", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}