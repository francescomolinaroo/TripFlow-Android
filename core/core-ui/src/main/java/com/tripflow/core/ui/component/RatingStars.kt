package com.tripflow.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.TripFlowColors
import java.util.Locale

private val StarPoints = listOf(
    12f to 3.5f, 14.6f to 8.8f, 20.5f to 9.7f, 16.2f to 13.8f, 17.2f to 19.6f,
    12f to 16.9f, 6.8f to 19.6f, 7.8f to 13.8f, 3.5f to 9.7f, 9.4f to 8.8f,
)

@Composable
fun Star(
    filled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 15.dp,
    color: Color = TripFlowColors.Star,
    emptyColor: Color = TripFlowColors.StarEmpty,
) {
    Canvas(modifier = modifier.size(size)) {
        val scale = this.size.minDimension / 24f
        val path = Path().apply {
            StarPoints.forEachIndexed { index, (x, y) ->
                if (index == 0) moveTo(x * scale, y * scale) else lineTo(x * scale, y * scale)
            }
            close()
        }
        drawPath(path, color = if (filled) color else emptyColor)
    }
}

@Composable
fun RatingStars(
    rating: Double?,
    modifier: Modifier = Modifier,
    starSize: Dp = 15.dp,
    reviewCount: Int? = null,
    showValue: Boolean = true,
) {
    if (rating == null) {
        Text(
            text = "Nessuna recensione",
            style = MaterialTheme.typography.bodySmall,
            color = TripFlowColors.TextSecondary,
            modifier = modifier,
        )
        return
    }

    val descrizione = buildString {
        append("Valutazione ")
        append(String.format(Locale.ITALY, "%.1f", rating))
        append(" su 5")
        if (reviewCount != null) {
            append(", ")
            append(if (reviewCount == 1) "1 recensione" else "$reviewCount recensioni")
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier.clearAndSetSemantics { contentDescription = descrizione },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            val piene = rating.toInt()
            repeat(5) { index ->
                Star(filled = index < piene, size = starSize)
            }
        }
        if (showValue) {
            Text(
                text = String.format(Locale.ITALY, "%.1f", rating),
                style = MaterialTheme.typography.titleSmall,
                color = TripFlowColors.TextPrimary,
            )
        }
        if (reviewCount != null) {
            Text(
                text = "($reviewCount)",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextSecondary,
            )
        }
    }
}

@Composable
fun RatingPicker(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        repeat(5) { index ->
            val value = index + 1
            val etichetta = if (value == 1) "1 stella" else "$value stelle"
            Star(
                filled = value <= rating,
                size = 44.dp,
                modifier = Modifier
                    .semantics { contentDescription = etichetta }
                    .clickable(role = Role.Button) { onRatingChange(value) },
            )
        }
    }
}
