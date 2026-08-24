package com.tripflow.feature.booking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.tripflow.feature.booking.ui.ActivityUi

@Composable
fun ActivityItem(activity: ActivityUi, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = if (activity.isSelected) 1.5.dp else 1.dp,
                color = if (activity.isSelected) TripFlowColors.Accent else TripFlowColors.Border,
                shape = shape
            )
            .background(if (activity.isSelected) TripFlowColors.AccentSoft else Color.Transparent)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = activity.isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = TripFlowColors.Accent)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.name, style = MaterialTheme.typography.titleSmall)
            Text(activity.duration, style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("€ ${activity.price}", style = MaterialTheme.typography.titleSmall)
            Text("a persona", style = MaterialTheme.typography.labelSmall, color = TripFlowColors.TextSecondary)
        }
    }
}