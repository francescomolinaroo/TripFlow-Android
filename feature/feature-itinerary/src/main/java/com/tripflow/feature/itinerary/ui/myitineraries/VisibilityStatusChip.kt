package com.tripflow.feature.itinerary.ui.myitineraries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.itinerary.model.Visibility

@Composable
fun VisibilityStatusChip(
    visibility: Visibility,
    modifier: Modifier = Modifier
) {
    val label = when (visibility) {
        Visibility.PUBLIC -> "Pubblico"
        Visibility.PRIVATE -> "Privato"
        Visibility.SHARED -> "Condiviso"
    }
    val icon = when (visibility) {
        Visibility.PUBLIC -> Icons.Default.Public
        Visibility.PRIVATE -> Icons.Default.Lock
        Visibility.SHARED -> Icons.Default.Share
    }
    val contentColor = when (visibility) {
        Visibility.PUBLIC -> TripFlowColors.Success
        Visibility.PRIVATE -> TripFlowColors.TextSecondary
        Visibility.SHARED -> TripFlowColors.Accent
    }
    val containerColor = when (visibility) {
        Visibility.PUBLIC -> TripFlowColors.SuccessSoft
        Visibility.PRIVATE -> TripFlowColors.Surface3
        Visibility.SHARED -> TripFlowColors.AccentSoft
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(22.dp)
            .background(containerColor, RoundedCornerShape(Dimens.radiusChip))
            .padding(horizontal = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontSize = 10.sp
            )
        }
    }
}
