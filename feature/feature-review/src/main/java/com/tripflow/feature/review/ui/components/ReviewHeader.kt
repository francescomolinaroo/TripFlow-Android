package com.tripflow.feature.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.component.RatingStars
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.feature.review.ui.components.RatingBar

@Composable
fun ReviewHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        Text("Costa Amalfitana", style = MaterialTheme.typography.bodyMedium, color = TripFlowColors.TextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("4.8", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                RatingStars(rating = 4.8, showValue = false)
                Text("23 recensioni", style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RatingBar(5, 18, 23)
                RatingBar(4, 3, 23)
                RatingBar(3, 2, 23)
                RatingBar(2, 0, 23)
                RatingBar(1, 0, 23)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = Dimens.gapL), color = TripFlowColors.Divider)
    }
}