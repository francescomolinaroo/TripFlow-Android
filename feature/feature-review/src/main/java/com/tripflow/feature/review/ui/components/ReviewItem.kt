package com.tripflow.feature.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.component.RatingStars
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun ReviewItem(review: ReviewUi) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserBadge(name = review.name, size = 32.dp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RatingStars(rating = review.rating, showValue = false, starSize = 12.dp)
                Text(review.date, style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(review.title, style = MaterialTheme.typography.titleMedium)
            if (review.isModified) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.radiusChip))
                        .background(TripFlowColors.Surface2)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("modificata", style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
                }
            }
        }
        Text(review.comment, style = MaterialTheme.typography.bodyMedium, color = TripFlowColors.TextBody)
    }
}