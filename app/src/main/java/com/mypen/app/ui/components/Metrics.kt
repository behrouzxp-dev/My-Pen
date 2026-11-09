package com.mypen.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mypen.app.ui.theme.Motion
import com.mypen.app.ui.theme.Radius
import com.mypen.app.ui.theme.Spacing
import com.mypen.app.util.PersianText

/**
 * Counter that animates from zero to [value] the first time it is shown, which
 * makes the dashboard feel alive without being distracting.
 */
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displaySmall
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animated by animateIntAsState(
        targetValue = if (started) value else 0,
        animationSpec = tween(durationMillis = Motion.COUNTER, easing = FastOutSlowInEasing),
        label = "metric-counter"
    )
    Text(
        text = PersianText.number(animated),
        modifier = modifier,
        color = color,
        style = style
    )
}

/**
 * Metric tile used on the dashboard. The accent is carried by the icon and the
 * value; the container itself stays neutral.
 */
@Composable
fun MetricCard(
    icon: ImageVector,
    label: String,
    value: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    caption: String? = null,
    onClick: (() -> Unit)? = null
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            AnimatedCounter(
                value = value,
                color = accent,
                style = MaterialTheme.typography.headlineMedium
            )
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Circular countdown: a ring whose sweep represents the remaining shelf life.
 * Drawn with Canvas so no extra dependency is required.
 */
@Composable
fun CountdownRing(
    fractionRemaining: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
    strokeWidth: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    val animated by animateFloatAsState(
        targetValue = fractionRemaining.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = Motion.LONG, easing = FastOutSlowInEasing),
        label = "countdown-ring"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = accent.copy(alpha = 0.16f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        content()
    }
}

/** Thin linear progress bar used for the remaining shelf life of a pen. */
@Composable
fun ShelfLifeBar(
    fractionElapsed: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    height: Dp = 5.dp
) {
    val animated by animateFloatAsState(
        targetValue = fractionElapsed.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = Motion.LONG, easing = FastOutSlowInEasing),
        label = "shelf-life"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(Radius.pill))
            .background(accent.copy(alpha = 0.16f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(Radius.pill))
                .background(accent)
        )
    }
}

/** Three-bar consumption priority indicator. */
@Composable
fun PriorityIndicator(
    priority: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val level = priority.coerceIn(1, 3)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (index in 1..3) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((5 + index * 4).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index <= (4 - level)) accent else accent.copy(alpha = 0.18f))
            )
        }
    }
}
