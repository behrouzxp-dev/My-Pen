package com.mypen.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.mypen.app.ui.theme.Motion
import com.mypen.app.ui.theme.Radius
import com.mypen.app.ui.theme.Spacing

/**
 * Lightweight shimmer placeholder shown while the first database read is in
 * flight, instead of a spinner on an empty page.
 */
@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = Motion.standard),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton-alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SkeletonBlock(height = 120.dp, alpha = pulse)
        SkeletonBlock(height = 88.dp, alpha = pulse)
        SkeletonBlock(height = 88.dp, alpha = pulse)
        SkeletonBlock(height = 140.dp, alpha = pulse)
    }
}

@Composable
private fun SkeletonBlock(height: androidx.compose.ui.unit.Dp, alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .alpha(alpha)
            .clip(RoundedCornerShape(Radius.lg))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}
