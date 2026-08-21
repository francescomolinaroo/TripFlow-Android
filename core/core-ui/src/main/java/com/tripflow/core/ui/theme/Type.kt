package com.tripflow.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val Family = FontFamily.Default

internal val TripFlowTypography = Typography(

    headlineMedium = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 25.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.02).em,
    ),

    headlineSmall = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.02).em,
    ),

    titleLarge = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.01).em,
    ),

    titleMedium = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),

    titleSmall = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),

    bodyLarge = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 21.7.sp,
    ),

    bodySmall = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.5.sp,
        lineHeight = 16.sp,
    ),

    labelSmall = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.09.em,
    ),
)
