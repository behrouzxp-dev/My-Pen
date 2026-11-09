package com.mypen.app.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Static dark palette.
 *
 * The brand colour is the deep navy blue used by the application launcher icon
 * (#305892). The interface itself is built from a small, disciplined set of
 * neutral navy surfaces; saturated colour is reserved for status and for
 * interactive elements, never for decoration.
 */

// Brand
val BrandNavy = Color(0xFF305892)
val BrandNavyDeep = Color(0xFF1B3350)

// Accent (interactive elements on dark surfaces)
val AccentBlue = Color(0xFF7CB6F2)
val AccentBluePressed = Color(0xFF9CC9F7)
val AccentTeal = Color(0xFF57D9C6)
val AccentViolet = Color(0xFFB5A8FF)

// Neutral surface scale (lowest = furthest back, highest = closest to user)
val SurfaceLowest = Color(0xFF070C14)
val SurfaceLow = Color(0xFF0E1723)
val SurfaceBase = Color(0xFF111A27)
val SurfaceContainerColor = Color(0xFF141F2E)
val SurfaceHigh = Color(0xFF1A2739)
val SurfaceHighest = Color(0xFF213044)
val BackgroundBase = Color(0xFF0A111C)

// Foreground
val OnSurfaceColor = Color(0xFFE9EFF7)
val OnSurfaceMuted = Color(0xFFA6B6C9)
val OnSurfaceFaint = Color(0xFF7E8FA5)
val OutlineColor = Color(0xFF3A4A5F)
val OutlineVariantColor = Color(0xFF243244)

// Status (semantic)
val StatusActive = Color(0xFF4ADE80)
val StatusActiveBg = Color(0xFF10281A)
val StatusExpiringSoon = Color(0xFFFBBF24)
val StatusExpiringSoonBg = Color(0xFF2E2209)
val StatusExpired = Color(0xFFF87171)
val StatusExpiredBg = Color(0xFF2E1214)
val StatusOpened = Color(0xFF60A5FA)
val StatusOpenedBg = Color(0xFF102138)
val StatusDefective = Color(0xFFA78BFA)
val StatusDefectiveBg = Color(0xFF1F1733)

// Insulin types
val TypeLongActing = Color(0xFF5EC8F0)
val TypeLongActingBg = Color(0xFF0B2430)
val TypeShortActing = Color(0xFFC084FC)
val TypeShortActingBg = Color(0xFF241238)

/*
 * Compatibility aliases.
 *
 * Older screens referenced these names directly. They are kept so that no
 * module breaks while the UI is migrated onto the semantic palette above, and
 * they will be removed once the migration is finished.
 */
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val PrimaryBlue = AccentBlue
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val PrimaryDarkBlue = AccentBlue
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val PrimaryLightBlue = BrandNavyDeep
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val SecondaryTeal = AccentTeal
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val BackgroundDark = BackgroundBase
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val SurfaceDark = SurfaceBase
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val SurfaceVariantDark = SurfaceContainerColor
@Deprecated("Use MaterialTheme.colorScheme.roles or the semantic tokens above.")
val OnPrimaryColor = Color(0xFF06121F)
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val DividerColor = OutlineVariantColor
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val BackgroundLight = BackgroundBase
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val SurfaceWhite = SurfaceBase
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val TextPrimary = OnSurfaceColor
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val TextSecondary = OnSurfaceMuted
@Deprecated("Use MaterialTheme.colorScheme roles or the semantic tokens above.")
val TextHint = OnSurfaceFaint
