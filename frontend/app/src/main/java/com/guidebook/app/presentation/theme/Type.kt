package com.guidebook.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Android 12+ ships Roboto Flex (Inter-derived variable font) as the system SansSerif.
// Older versions get Roboto — still excellent. No binary font assets needed.
private val AppFont = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-1.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 30.sp,
        lineHeight    = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 26.sp,
        lineHeight    = 34.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineLarge = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 30.sp,
        lineHeight    = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 26.sp,
        lineHeight    = 32.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 20.sp,
        lineHeight    = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 17.sp,
        lineHeight    = 24.sp,
        letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = AppFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.3.sp
    ),
)
