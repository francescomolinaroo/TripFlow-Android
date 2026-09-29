package com.tripflow.feature.itinerary.ui.addstage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors
import com.tripflow.core.ui.theme.TripFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStageScreen(itineraryId: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aggiungi Tappa", style = androidx.compose.material3.MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TripFlowColors.Background)
            )
        },
        containerColor = TripFlowColors.Background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.screenPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AddStageScreen - TODO\nItinerario: $itineraryId",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = TripFlowColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}