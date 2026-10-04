package com.tripflow.feature.review.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.ui.component.StateHost
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.review.ui.components.ReviewHeader
import com.tripflow.feature.review.ui.components.ReviewItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    oggettoId: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: ReviewListViewModel = viewModel(key = oggettoId ?: "mie") { ReviewListViewModel(oggettoId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadReviews()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isMine) "Le mie recensioni" else "Recensioni", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        StateHost(
            state = uiState.reviews,
            onRetry = viewModel::loadReviews,
            modifier = Modifier.padding(innerPadding),
            emptyTitle = if (uiState.isMine) "Nessuna recensione" else "Ancora nessuna recensione"
        ) { reviews ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapXL)
            ) {
                val summary = uiState.summary
                if (!uiState.isMine && summary != null) {
                    item {
                        ReviewHeader(subjectName = uiState.subjectName, summary = summary)
                    }
                }

                items(reviews) { review ->
                    ReviewItem(review = review)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewListScreenPreview() {
    TripFlowTheme {
        ReviewListScreen()
    }
}
