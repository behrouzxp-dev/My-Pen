package com.mypen.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Only the stable Material 3 colour roles are set here. The extended surface
 * container tones are exposed through [SurfaceLow], [SurfaceContainerColor] and
 * [SurfaceHigh] (see Color.kt) so the UI does not depend on newer Material 3
 * colour slot APIs.
 */
private val StaticDarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color(0xFF06121F),
    primaryContainer = BrandNavyDeep,
    onPrimaryContainer = Color(0xFFCFE4FF),
    inversePrimary = BrandNavy,
    secondary = AccentTeal,
    onSecondary = Color(0xFF04211D),
    secondaryContainer = Color(0xFF10312E),
    onSecondaryContainer = Color(0xFFC7F6EF),
    tertiary = AccentViolet,
    onTertiary = Color(0xFF120A2E),
    tertiaryContainer = Color(0xFF241C4A),
    onTertiaryContainer = Color(0xFFE2DBFF),
    background = BackgroundBase,
    onBackground = OnSurfaceColor,
    surface = SurfaceBase,
    onSurface = OnSurfaceColor,
    surfaceVariant = SurfaceContainerColor,
    onSurfaceVariant = OnSurfaceMuted,
    surfaceTint = AccentBlue,
    inverseSurface = OnSurfaceColor,
    inverseOnSurface = SurfaceLow,
    error = StatusExpired,
    onError = Color(0xFF2A0707),
    errorContainer = StatusExpiredBg,
    onErrorContainer = Color(0xFFFFD7D7),
    outline = OutlineColor,
    outlineVariant = OutlineVariantColor,
    scrim = Color(0xFF000000)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(Radius.xl)
)

/**
 * Permanent static dark theme with RTL layout direction and Vazirmatn
 * typography. Theme switching (light/system) is intentionally not supported and
 * dynamic colour is deliberately not used, so the interface looks identical on
 * every device.
 */
@Composable
fun MyPenTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = StaticDarkColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
