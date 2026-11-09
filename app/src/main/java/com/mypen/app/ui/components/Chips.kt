package com.mypen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mypen.app.model.EventType
import com.mypen.app.model.InsulinType
import com.mypen.app.model.PenStatus
import com.mypen.app.ui.theme.Radius
import com.mypen.app.ui.theme.eventBackground
import com.mypen.app.ui.theme.eventColor
import com.mypen.app.ui.theme.statusBackground
import com.mypen.app.ui.theme.statusColor
import com.mypen.app.ui.theme.typeBackground
import com.mypen.app.ui.theme.typeColor

/**
 * Small, consistent status pills. They replace the previous per-screen
 * `Surface + BorderStroke` duplication and keep colour usage disciplined: the
 * fill is a light tint of the accent and the label carries the accent.
 */
@Composable
fun AppPill(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    background: Color = Color.Unspecified,
    showDot: Boolean = true
) {
    val fill = if (background == Color.Unspecified) accent.copy(alpha = 0.14f) else background
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(fill)
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(Radius.pill))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            maxLines = 1
        )
    }
}

@Composable
fun StatusChip(status: PenStatus, modifier: Modifier = Modifier) {
    AppPill(
        text = status.persianLabel,
        accent = statusColor(status),
        background = statusBackground(status),
        modifier = modifier
    )
}

@Composable
fun TypeChip(type: InsulinType, modifier: Modifier = Modifier) {
    AppPill(
        text = type.persianLabel,
        accent = typeColor(type),
        background = typeBackground(type),
        modifier = modifier
    )
}

@Composable
fun EventChip(eventType: EventType, modifier: Modifier = Modifier) {
    AppPill(
        text = eventType.persianLabel,
        accent = eventColor(eventType),
        background = eventBackground(eventType),
        modifier = modifier
    )
}
