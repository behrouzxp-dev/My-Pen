package com.mypen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mypen.app.R
import com.mypen.app.model.DashboardMetrics
import com.mypen.app.model.InsulinPen
import com.mypen.app.model.InsulinType
import com.mypen.app.ui.components.AnimatedCounter
import com.mypen.app.ui.components.AppCard
import com.mypen.app.ui.components.AppPill
import com.mypen.app.ui.components.EmptyState
import com.mypen.app.ui.components.MetricCard
import com.mypen.app.ui.components.CountdownRing
import com.mypen.app.ui.components.SectionHeader
import com.mypen.app.ui.components.ShelfLifeBar
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils
import com.mypen.app.util.PersianText

@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    activePens: List<InsulinPen>,
    today: String,
    onNavigateToInventory: (InsulinType) -> Unit,
    onNavigateToExpired: () -> Unit,
    onAddPen: () -> Unit,
    onPenSelected: (String) -> Unit
) {
    val ordered = remember(activePens) {
        activePens.sortedBy { it.expiryDate }
    }
    val urgentPen = ordered.firstOrNull()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWide = maxWidth >= 640.dp
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.sm,
                bottom = Spacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
        // 1. Expiring-soon banner
        if (metrics.expiringSoonCount > 0) {
            item("banner") {
                ExpiringBanner(
                    count = metrics.expiringSoonCount,
                    onClick = { onNavigateToInventory(InsulinType.LONG_ACTING) }
                )
            }
        }

        // 2. Hero: the pen that should be used first
        item("hero") {
            SectionHeader(title = stringResource(R.string.dashboard_priority_section))
        }
        item("hero_card") {
            if (urgentPen != null) {
                UrgentPenCard(
                    pen = urgentPen,
                    today = today,
                    onClick = { onPenSelected(urgentPen.id) }
                )
            } else {
                AppCard {
                    EmptyState(
                        icon = Icons.Filled.Inventory2,
                        title = stringResource(R.string.dashboard_empty_title),
                        description = stringResource(R.string.dashboard_empty_desc),
                        actionLabel = stringResource(R.string.btn_add_pen),
                        onAction = onAddPen
                    )
                }
            }
        }

        // 3. Totals
        item("totals") {
            AppCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.total_active_pens),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AnimatedCounter(
                            value = metrics.totalActive,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = stringResource(R.string.dashboard_ready_to_use),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // 4-5. Metrics: one row of four on wide windows, two rows of two on phones
        if (isWide) {
            item("metrics_wide") {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    MetricCard(
                        icon = Icons.Filled.Schedule,
                        label = stringResource(R.string.long_acting_pens),
                        value = metrics.longActingCount,
                        accent = typeColor(InsulinType.LONG_ACTING),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToInventory(InsulinType.LONG_ACTING) }
                    )
                    MetricCard(
                        icon = Icons.Filled.Schedule,
                        label = stringResource(R.string.short_acting_pens),
                        value = metrics.shortActingCount,
                        accent = typeColor(InsulinType.SHORT_ACTING),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToInventory(InsulinType.SHORT_ACTING) }
                    )
                    MetricCard(
                        icon = Icons.Filled.Warning,
                        label = stringResource(R.string.expiring_soon_short),
                        value = metrics.expiringSoonCount,
                        accent = StatusExpiringSoon,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToInventory(InsulinType.LONG_ACTING) }
                    )
                    MetricCard(
                        icon = Icons.Filled.EventBusy,
                        label = stringResource(R.string.expired_pens),
                        value = metrics.expiredCount,
                        accent = StatusExpired,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExpired
                    )
                }
            }
        } else {
            item("types") {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    MetricCard(
                        icon = Icons.Filled.Schedule,
                        label = stringResource(R.string.long_acting_pens),
                        value = metrics.longActingCount,
                        accent = typeColor(InsulinType.LONG_ACTING),
                        caption = stringResource(R.string.metric_tap_for_inventory),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToInventory(InsulinType.LONG_ACTING) }
                    )
                    MetricCard(
                        icon = Icons.Filled.Schedule,
                        label = stringResource(R.string.short_acting_pens),
                        value = metrics.shortActingCount,
                        accent = typeColor(InsulinType.SHORT_ACTING),
                        caption = stringResource(R.string.metric_tap_for_inventory),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToInventory(InsulinType.SHORT_ACTING) }
                    )
                }
            }
            item("risk") {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    MetricCard(
                        icon = Icons.Filled.Warning,
                        label = stringResource(R.string.expiring_soon_short),
                        value = metrics.expiringSoonCount,
                        accent = StatusExpiringSoon,
                        caption = stringResource(R.string.metric_within_30_days),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToInventory(InsulinType.LONG_ACTING) }
                    )
                    MetricCard(
                        icon = Icons.Filled.EventBusy,
                        label = stringResource(R.string.expired_pens),
                        value = metrics.expiredCount,
                        accent = StatusExpired,
                        caption = stringResource(R.string.metric_review_now),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExpired
                    )
                }
            }
        }

        // 6. Expiry distribution insight
        if (ordered.isNotEmpty()) {
            item("insight_header") {
                SectionHeader(title = stringResource(R.string.dashboard_insight_section))
            }
            item("insight") {
                ExpiryDistributionCard(pens = ordered, today = today)
            }
        }

        // 7. Quick actions
        item("actions_header") {
            SectionHeader(title = stringResource(R.string.quick_actions))
        }
        item("actions") {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Button(
                    onClick = onAddPen,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(Radius.md)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = stringResource(R.string.btn_add_pen),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedButton(
                    onClick = { onNavigateToInventory(InsulinType.LONG_ACTING) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(Radius.md)
                ) {
                    Text(
                        text = stringResource(R.string.btn_view_inventory),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ExpiringBanner(count: Int, onClick: () -> Unit) {
    AppCard(containerColor = StatusExpiringSoonBg, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = StatusExpiringSoon,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.expiring_soon_alert_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = StatusExpiringSoon
                )
                Text(
                    text = stringResource(R.string.expiring_soon_alert_desc, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UrgentPenCard(pen: InsulinPen, today: String, onClick: () -> Unit) {
    val daysRemaining = DateUtils.calculateDaysRemaining(pen.expiryDate, today)
    val accent = expiryAccent(daysRemaining)
    val fraction = (daysRemaining.toFloat() / 90f).coerceIn(0f, 1f)

    AppCard(onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CountdownRing(
                    fractionRemaining = fraction,
                    accent = accent,
                    size = 88.dp,
                    strokeWidth = 7.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (daysRemaining > 0L) PersianText.number(daysRemaining) else "۰",
                            style = MaterialTheme.typography.titleLarge,
                            color = accent
                        )
                        Text(
                            text = stringResource(R.string.unit_day),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Spacing.lg))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = pen.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        AppPill(
                            text = pen.type.persianLabel,
                            accent = typeColor(pen.type),
                            background = typeBackground(pen.type)
                        )
                        AppPill(
                            text = stringResource(R.string.priority_short, PersianText.number(pen.priority)),
                            accent = accent,
                            showDot = false
                        )
                    }
                    Text(
                        text = DateUtils.remainingLabel(daysRemaining),
                        style = MaterialTheme.typography.bodySmall,
                        color = accent
                    )
                }
            }

            ShelfLifeBar(
                fractionElapsed = DateUtils.shelfLifeProgress(pen.entryDate, pen.expiryDate, today),
                accent = accent
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_expiry_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = DateUtils.displayDateLong(pen.expiryDate),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ExpiryDistributionCard(pens: List<InsulinPen>, today: String) {
    val bucketSize = 15
    val buckets = remember(pens, today) {
        val counts = IntArray(6)
        pens.forEach { pen ->
            val days = DateUtils.calculateDaysRemaining(pen.expiryDate, today)
            if (days < 0L) {
                counts[0]++
            } else {
                val index = (days / bucketSize).toInt().coerceIn(0, 5)
                counts[index]++
            }
        }
        counts
    }
    val maxCount = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)

    AppCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = stringResource(R.string.insight_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.insight_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                buckets.forEachIndexed { index, count ->
                    val barAccent = when (index) {
                        0, 1 -> StatusExpired
                        2, 3 -> StatusExpiringSoon
                        else -> StatusActive
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = PersianText.number(count),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (count == 0) MaterialTheme.colorScheme.onSurfaceVariant else barAccent
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(((count.toFloat() / maxCount) * 56f + 6f).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                .background(barAccent.copy(alpha = if (count == 0) 0.18f else 0.85f))
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = PersianText.number((index + 1) * bucketSize),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.insight_footnote),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
