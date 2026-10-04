package com.tripflow.feature.review.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.ui.component.PrimaryButton
import com.tripflow.core.ui.component.RatingPicker
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.component.TripFlowTextArea
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.review.ui.components.TripSmallHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    prenotazioneId: String? = null,
    viaggioId: String? = null,
    tripTitle: String = "Viaggio",
    tripSubtitle: String? = null,
    onBack: () -> Unit = {},
    onPublish: () -> Unit = {},
    viewModel: WriteReviewViewModel = viewModel(key = prenotazioneId) {
        WriteReviewViewModel(prenotazioneId = prenotazioneId, oggettoId = viaggioId)
    }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isPublished) {
        if (uiState.isPublished) {
            onPublish()
            viewModel.onPublishedHandled()
        }
    }

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
                uiState.error?.let { error ->
                    Text(error, style = MaterialTheme.typography.bodySmall, color = TripFlowColors.Error)
                }
                PrimaryButton(
                    text = if (uiState.isLoading) "Pubblicazione..." else "Pubblica recensione",
                    enabled = !uiState.isLoading,
                    onClick = viewModel::publishReview
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
            TripSmallHeader(title = tripTitle, subtitle = tripSubtitle)

            Text(
                "Com'è andata?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            RatingPicker(
                rating = uiState.rating,
                onRatingChange = { viewModel.onRatingChange(it) }
            )
            
            Text(
                text = uiState.feedbackText,
                style = MaterialTheme.typography.titleMedium,
                color = TripFlowColors.Warning
            )

            TripFlowTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = "Titolo",
                placeholder = "Es: Un viaggio indimenticabile",
                error = uiState.titleError
            )

            TripFlowTextArea(
                value = uiState.comment,
                onValueChange = { viewModel.onCommentChange(it) },
                label = "Commento",
                placeholder = "Racconta la tua esperienza...",
                maxChars = 5000,
                error = uiState.commentError
            )
            
            Spacer(modifier = Modifier.height(100.dp))
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
