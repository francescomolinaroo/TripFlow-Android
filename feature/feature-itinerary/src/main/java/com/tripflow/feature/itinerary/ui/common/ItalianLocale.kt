package com.tripflow.feature.itinerary.ui.common

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

// Forza l'italiano per il contenuto (es. DatePicker): nomi di mesi e giorni e testi
// Material del calendario restano in italiano anche se il telefono e' in inglese.
@Composable
fun WithItalianLocale(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val italianConfiguration = remember(configuration) {
        Configuration(configuration).apply { setLocale(Locale.ITALIAN) }
    }
    val italianContext = remember(context, italianConfiguration) {
        context.createConfigurationContext(italianConfiguration)
    }
    CompositionLocalProvider(
        LocalConfiguration provides italianConfiguration,
        LocalContext provides italianContext,
    ) {
        content()
    }
}
