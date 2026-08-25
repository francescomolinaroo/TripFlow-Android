package com.tripflow.feature.review.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.review.ui.components.ReviewHeader
import com.tripflow.feature.review.ui.components.ReviewItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    onBackClick: () -> Unit = {},
    viewModel: ReviewListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recensioni", style = MaterialTheme.typography.titleLarge) },
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

            items(uiState.reviews) { review ->
                ReviewItem(review = review)
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
