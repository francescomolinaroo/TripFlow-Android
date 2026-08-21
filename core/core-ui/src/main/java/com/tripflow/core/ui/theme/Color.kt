package com.tripflow.core.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object TripFlowColors {

    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFAFBFC)
    val Surface2 = Color(0xFFF6F7F9)
    val Surface3 = Color(0xFFF1F3F6)
    val Border = Color(0xFFE4E7EC)
    val Divider = Color(0xFFF1F3F6)

    val TextPrimary = Color(0xFF101319)
    val TextBody = Color(0xFF414A57)
    val TextSecondary = Color(0xFF8A93A1)
    val TextDisabled = Color(0xFFC4CAD3)

    val Accent = Color(0xFF4F46E5)
    val AccentDark = Color(0xFF4338CA)
    val AccentSoft = Color(0xFFEEF0FE)
    val AccentBorder = Color(0xFFDDDFFB)

    val Success = Color(0xFF15803D)
    val SuccessSoft = Color(0xFFE7F6EC)
    val Warning = Color(0xFFB45309)
    val WarningSoft = Color(0xFFFEF3C7)
    val Error = Color(0xFFC0392B)
    val ErrorSoft = Color(0xFFFDECEA)
    val ErrorBorder = Color(0xFFF0D6D2)
    val FieldError = Color(0xFFD14343)

    val Star = Color(0xFFF59E0B)
    val StarEmpty = Color(0xFFE0E3E9)
}

internal val TripFlowColorScheme = lightColorScheme(
    primary = TripFlowColors.Accent,
    onPrimary = Color.White,
    primaryContainer = TripFlowColors.AccentSoft,
    onPrimaryContainer = TripFlowColors.AccentDark,

    secondary = TripFlowColors.TextPrimary,
    onSecondary = Color.White,

    background = TripFlowColors.Background,
    onBackground = TripFlowColors.TextPrimary,

    surface = TripFlowColors.Background,
    onSurface = TripFlowColors.TextPrimary,
    surfaceVariant = TripFlowColors.Surface2,
    onSurfaceVariant = TripFlowColors.TextSecondary,

    outline = TripFlowColors.Border,
    outlineVariant = TripFlowColors.Divider,

    error = TripFlowColors.Error,
    onError = Color.White,
    errorContainer = TripFlowColors.ErrorSoft,
    onErrorContainer = TripFlowColors.Error,
)
