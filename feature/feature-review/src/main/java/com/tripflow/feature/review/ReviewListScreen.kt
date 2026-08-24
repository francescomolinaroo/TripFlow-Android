package com.tripflow.feature.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@Composable
fun ReviewListScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(containerColor = TripFlowColors.Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapL),
            horizontalAlignment = Alignment.Start
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
            }
            Text("Le mie recensioni", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Qui vedrai le recensioni che hai scritto.",
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewListScreenPreview() {
    TripFlowTheme { ReviewListScreen() }
}