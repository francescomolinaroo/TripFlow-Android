package com.tripflow.feature.booking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapM)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TripFlowColors.TextSecondary,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
        )
        content()
    }
}