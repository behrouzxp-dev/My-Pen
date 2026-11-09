package com.mypen.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mypen.app.R

val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val DefaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = VazirmatnFontFamily),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = VazirmatnFontFamily),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = VazirmatnFontFamily),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = VazirmatnFontFamily),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = VazirmatnFontFamily),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = VazirmatnFontFamily),
    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = VazirmatnFontFamily),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = VazirmatnFontFamily),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = VazirmatnFontFamily),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = VazirmatnFontFamily),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = VazirmatnFontFamily),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = VazirmatnFontFamily),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = VazirmatnFontFamily)
)
