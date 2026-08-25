package com.tripflow.feature.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun RatingBar(stars: Int, count: Int, total: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stars.toString(), style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
        LinearProgressIndicator(
            progress = { if (total > 0) count.toFloat() / total else 0f },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = TripFlowColors.Star,
            trackColor = TripFlowColors.Surface2,
        )
        Text(count.toString(), style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary, modifier = Modifier.width(16.dp))
    }
}