package com.mypen.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val StaticDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryColor,
    primaryContainer = PrimaryLightBlue,
    onPrimaryContainer = PrimaryDarkBlue,
    secondary = SecondaryTeal,
    onSecondary = OnPrimaryColor,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = StatusExpired,
    onError = OnPrimaryColor
)

/**
 * Permanent static dark theme with RTL layout direction and Vazirmatn typography.
 * Theme switching (light/system) is intentionally not supported.
 */
@Composable
fun MyPenTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = StaticDarkColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
