package com.tripflow.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object Dimens {
    val screenPadding = 20.dp
    val gapS = 8.dp
    val gapM = 12.dp
    val gapL = 16.dp
    val gapXL = 24.dp

    val radiusChip = 8.dp
    val radiusField = 14.dp
    val radiusButton = 14.dp
    val radiusRow = 16.dp
    val radiusCard = 18.dp

    val buttonHeight = 52.dp
    val fieldHeight = 50.dp
    val rowHeight = 62.dp
    val bottomBarHeight = 68.dp
    val minTouchTarget = 44.dp
}

@Composable
fun TripFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TripFlowColorScheme,
        typography = TripFlowTypography,
        content = content,
    )
}
