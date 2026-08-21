package com.tripflow.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun UserBadge(
    name: String?,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    caption: String? = null,
    size: Dp = 44.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        Avatar(name = name, imageUrl = imageUrl, size = size)

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (caption != null) {
                Text(
                    text = caption.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TripFlowColors.TextSecondary,
                )
            }
            Text(
                text = name ?: "…",
                style = MaterialTheme.typography.titleSmall,
                color = TripFlowColors.TextPrimary,
            )
        }
    }
}

@Composable
fun Avatar(
    name: String?,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = 44.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(TripFlowColors.AccentSoft),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape),
            )
        } else {
            Text(
                text = initials(name),
                style = MaterialTheme.typography.titleSmall,
                color = TripFlowColors.AccentDark,
            )
        }
    }
}

private fun initials(name: String?): String {
    val parts = name?.trim()?.split(" ")?.filter { it.isNotBlank() }.orEmpty()
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}
