package com.mypen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mypen.app.model.InsulinType
import com.mypen.app.ui.theme.Radius
import com.mypen.app.ui.theme.typeColor

/**
 * Segmented control for the insulin type. Replaces the previous pair of radio
 * buttons; it is larger, easier to hit and keeps both options visible.
 */
@Composable
fun TypeSelector(
    selected: InsulinType,
    onSelected: (InsulinType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.pill))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(InsulinType.LONG_ACTING, InsulinType.SHORT_ACTING).forEach { type ->
            val isSelected = type == selected
            val accent = typeColor(type)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) accent.copy(alpha = 0.45f) else Color.Transparent,
                        shape = RoundedCornerShape(Radius.pill)
                    )
                    .clickable { onSelected(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.persianLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
