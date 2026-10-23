package com.tripflow.core.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object TripFlowColors {

    val FernGreen = Color(0xFF437118)
    val Pistacchio = Color(0xFFAFD06E)
    val Beige = Color(0xFFF5F3D8)
    val CarolinaBlue = Color(0xFF87AECE)

    val Background = Color(0xFFFBFAF5)
    val Surface = Beige
    val Surface2 = Color(0xFFEFECCF)
    val Surface3 = Color(0xFFE7E4C4)
    val Border = Color(0xFFDEDAB6)
    val Divider = Color(0xFFEAE7C8)

    val TextPrimary = Color(0xFF101319)
    val TextBody = Color(0xFF414A57)
    val TextSecondary = Color(0xFF7A8290)
    val TextDisabled = Color(0xFFB4B7AE)

    val Accent = FernGreen
    val AccentDark = Color(0xFF2F5010)
    val AccentSoft = Pistacchio
    val AccentBorder = Color(0xFFCFE2A0)

    val ButtonContainer = FernGreen
    val ButtonPressed = AccentDark
    val ButtonContent = Color.White
    val ButtonPressedContent = Color.White

    val StatusContainer = CarolinaBlue
    val StatusContent = TextPrimary

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
    primary = TripFlowColors.FernGreen,
    onPrimary = Color.White,
    primaryContainer = TripFlowColors.Pistacchio,
    onPrimaryContainer = TripFlowColors.TextPrimary,

    secondary = TripFlowColors.TextPrimary,
    onSecondary = Color.White,
    secondaryContainer = TripFlowColors.Pistacchio,
    onSecondaryContainer = TripFlowColors.TextPrimary,

    tertiary = TripFlowColors.CarolinaBlue,
    onTertiary = TripFlowColors.TextPrimary,

    background = TripFlowColors.Background,
    onBackground = TripFlowColors.TextPrimary,

    surface = TripFlowColors.Background,
    onSurface = TripFlowColors.TextPrimary,
    surfaceVariant = TripFlowColors.Surface2,
    onSurfaceVariant = TripFlowColors.TextSecondary,
    surfaceContainerLowest = TripFlowColors.Background,
    surfaceContainerLow = TripFlowColors.Background,
    surfaceContainer = TripFlowColors.Beige,
    surfaceContainerHigh = TripFlowColors.Background,
    surfaceContainerHighest = TripFlowColors.Surface3,

    outline = TripFlowColors.Border,
    outlineVariant = TripFlowColors.Divider,

    error = TripFlowColors.Error,
    onError = Color.White,
    errorContainer = TripFlowColors.ErrorSoft,
    onErrorContainer = TripFlowColors.Error,
)
