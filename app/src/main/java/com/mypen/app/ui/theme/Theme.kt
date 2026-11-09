package com.mypen.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryLightBlue,
    onPrimaryContainer = PrimaryDarkBlue,
    secondary = SecondaryTeal,
    onSecondary = SurfaceWhite,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun MyPenTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            content = content
        )
    }
}
