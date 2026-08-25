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

@Composable
fun VisibilityStatusChip(
    isPublic: Boolean,
    modifier: Modifier = Modifier
) {
    val label = if (isPublic) "PUBBLICO" else "PRIVATO"
    val icon = if (isPublic) Icons.Default.Public else Icons.Default.Lock
    val contentColor = if (isPublic) TripFlowColors.Success else TripFlowColors.TextSecondary
    val containerColor = if (isPublic) TripFlowColors.SuccessSoft else TripFlowColors.Surface3

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