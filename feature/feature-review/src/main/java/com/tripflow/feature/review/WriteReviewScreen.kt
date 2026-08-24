package com.tripflow.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.RatingPicker
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.component.TripFlowTextArea
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    onBack: () -> Unit = {},
    onPublish: () -> Unit = {}
) {
    var rating by remember { mutableIntStateOf(4) }
    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val titleError = if (submitted && title.isBlank()) "Inserisci un titolo per la recensione" else null
    val commentError = if (submitted && comment.isBlank()) "Racconta la tua esperienza" else null
    val formIsValid = title.isNotBlank() && comment.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scrivi una recensione", style = MaterialTheme.typography.titleLarge) },
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
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(Dimens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = "Pubblica recensione",
                    onClick = {
                        submitted = true
                        if (formIsValid) {
                            onPublish()
                        }
                    }
                )
                Text(
                    "Puoi recensire una sola volta per prenotazione.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.TextSecondary
                )
            }
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.gapXL)
        ) {
            // Trip Header
            TripSmallHeader()

            Text(
                "Com'è andata?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            RatingPicker(
                rating = rating,
                onRatingChange = { rating = it }
            )
            
            val feedback = when(rating) {
                1 -> "Pessimo"
                2 -> "Deludente"
                3 -> "Nella media"
                4 -> "Molto bello"
                5 -> "Eccellente"
                else -> ""
            }
            
            Text(feedback, style = MaterialTheme.typography.titleMedium, color = TripFlowColors.Warning)

            TripFlowTextField(
                value = title,
                onValueChange = { title = it },
                label = "Titolo",
                placeholder = "Es: Un viaggio indimenticabile",
                error = titleError
            )

            TripFlowTextArea(
                value = comment,
                onValueChange = { comment = it },
                label = "Commento",
                placeholder = "Racconta la tua esperienza...",
                maxChars = 5000,
                error = commentError
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun TripSmallHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(TripFlowColors.Surface)
            .padding(Dimens.gapM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapM)
    ) {
        AsyncImage(
            model = null,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(Dimens.radiusChip))
                .background(TripFlowColors.Surface2),
            contentScale = ContentScale.Crop
        )
        Column {
            Text("Isole Eolie in barca", style = MaterialTheme.typography.titleMedium)
            Text("Viaggio completato il 7 giu 2026", style = MaterialTheme.typography.bodySmall, color = TripFlowColors.TextSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WriteReviewScreenPreview() {
    TripFlowTheme {
        WriteReviewScreen()
    }
}
