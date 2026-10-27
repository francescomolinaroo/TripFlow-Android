package com.tripflow.feature.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.TripFlowColors

// Fascia a tutta larghezza in cima alle schermate di autenticazione: una foto di paesaggio
// che sfuma nel colore di fondo dell'app, con il logo TripFlow sopra.
// Il titolo della schermata resta sotto, nel form, dove si legge su fondo pulito.
@Composable
internal fun AuthHeader(
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Scurisce la parte alta (per il logo bianco) e sfuma nel fondo dell'app in basso.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.45f),
                        0.50f to Color.Black.copy(alpha = 0.30f),
                        0.78f to TripFlowColors.Background.copy(alpha = 0f),
                        1.0f to TripFlowColors.Background
                    )
                )
        )

        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Color.White)
            }
        }

        // Logo con la scritta, senza sfondo: colori chiari, quindi va sulla parte scurita della foto.
        Image(
            painter = painterResource(R.drawable.logo_tripflow),
            contentDescription = "TripFlow",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 20.dp)
                .height(56.dp)
        )
    }
}
