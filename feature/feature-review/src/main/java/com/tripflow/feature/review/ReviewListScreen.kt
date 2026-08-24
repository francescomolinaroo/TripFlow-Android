package com.tripflow.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.component.RatingStars
import com.tripflow.core.ui.component.UserBadge
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recensioni", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TripFlowColors.Background
                )
            )
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        val reviews = listOf(
            ReviewUi(
                name = "Giulia Rinaldi",
                rating = 5.0,
                date = "2 ago 2026",
                title = "Organizzazione impeccabile",
                comment = "Il sentiero degli Dei vale da solo il viaggio. Hotel ottimo, trasferimenti puntuali, gruppo piccolo."
            ),
            ReviewUi(
                name = "Andrea Moretti",
                rating = 4.0,
                date = "28 lug 2026",
                title = "Bello, ma tanti spostamenti",
                comment = "I trasferimenti in bus sono lunghi e le strade strette. Il tour in barca però lo rifarei domani.",
                isModified = true
            ),
            ReviewUi(
                name = "Luca Conti",
                rating = 5.0,
                date = "19 lug 2026",
                title = "Ci torno l'anno prossimo",
                comment = "Marco conosce ogni angolo della costiera."
            )
        )

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapXL)
        ) {
            item {
                ReviewHeader()
            }

            items(reviews) { review ->
                ReviewItem(review = review)
            }
        }
    }
}

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

data class ReviewUi(
    val name: String,
    val rating: Double,
    val date: String,
    val title: String,
    val comment: String,
    val isModified: Boolean = false
)

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

@Preview(showBackground = true)
@Composable
fun ReviewListScreenPreview() {
    TripFlowTheme {
        ReviewListScreen()
    }
}
