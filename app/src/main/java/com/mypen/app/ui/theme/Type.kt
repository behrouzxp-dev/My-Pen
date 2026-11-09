package com.mypen.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mypen.app.R

val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

/**
 * Persian typography scale.
 *
 * Vazirmatn is designed for Persian text: it needs generous line height and no
 * letter tracking. Applying Roboto's default tracking to Persian text makes it
 * look cramped and breaks the joining of letters, therefore every style sets
 * [TextStyle.letterSpacing] explicitly to zero.
 */
val AppTypography = Typography(
    displayLarge = AppTextStyle(40, 54, FontWeight.Bold),
    displayMedium = AppTextStyle(34, 48, FontWeight.Bold),
    displaySmall = AppTextStyle(30, 44, FontWeight.Bold),
    headlineLarge = AppTextStyle(26, 40, FontWeight.Bold),
    headlineMedium = AppTextStyle(22, 34, FontWeight.SemiBold),
    headlineSmall = AppTextStyle(20, 32, FontWeight.SemiBold),
    titleLarge = AppTextStyle(18, 30, FontWeight.SemiBold),
    titleMedium = AppTextStyle(16, 28, FontWeight.Medium),
    titleSmall = AppTextStyle(14, 24, FontWeight.Medium),
    bodyLarge = AppTextStyle(16, 28, FontWeight.Normal),
    bodyMedium = AppTextStyle(14, 26, FontWeight.Normal),
    bodySmall = AppTextStyle(12, 22, FontWeight.Normal),
    labelLarge = AppTextStyle(14, 22, FontWeight.Medium),
    labelMedium = AppTextStyle(12, 20, FontWeight.Medium),
    labelSmall = AppTextStyle(11, 18, FontWeight.Medium)
)

private fun AppTextStyle(sizeSp: Int, lineHeightSp: Int, weight: FontWeight): TextStyle {
    return TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = weight,
        fontSize = sizeSp.sp,
        lineHeight = lineHeightSp.sp,
        letterSpacing = 0.sp
    )
}
