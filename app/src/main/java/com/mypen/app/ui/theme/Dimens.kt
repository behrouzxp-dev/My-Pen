package com.mypen.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.unit.dp

/**
 * Spacing scale. Every gap, padding and inset in the application comes from
 * this scale (or a multiple of it) so that rhythm stays consistent.
 */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp
}

/** Corner radius scale. */
object Radius {
    val sm = 10.dp
    val md = 14.dp
    val lg = 18.dp
    val xl = 24.dp
    val pill = 100.dp
}

/** Motion tokens: keep animation durations and curves in one place. */
object Motion {
    const val SHORT = 150
    const val MEDIUM = 250
    const val LONG = 400
    const val COUNTER = 700

    val standard: Easing = FastOutSlowInEasing
    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
