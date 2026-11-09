package com.mypen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mypen.app.R
import com.mypen.app.model.EventType
import com.mypen.app.model.HistoryEvent
import com.mypen.app.ui.components.AppCard
import com.mypen.app.ui.components.EmptyState
import com.mypen.app.ui.components.EventChip
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils
import com.mypen.app.util.PersianText

@Composable
fun HistoryScreen(
    events: List<HistoryEvent>,
    today: String,
    selectedFilter: EventType?,
    onFilterSelected: (EventType?) -> Unit,
    onEventClicked: (String) -> Unit
) {
    val counts = remember(events) { events.groupingBy { it.eventType }.eachCount() }
    val groups = remember(events, today) {
        events.groupBy { DateUtils.relativeDayLabel(it.eventDate, today) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            HistoryFilterChip(
                label = stringResource(R.string.filter_all),
                count = null,
                selected = selectedFilter == null,
                accent = MaterialTheme.colorScheme.primary,
                onClick = { onFilterSelected(null) }
            )
            HistoryFilterChip(
                label = stringResource(R.string.filter_opened),
                count = counts[EventType.OPENED],
                selected = selectedFilter == EventType.OPENED,
                accent = eventColor(EventType.OPENED),
                onClick = { onFilterSelected(EventType.OPENED) }
            )
            HistoryFilterChip(
                label = stringResource(R.string.filter_defective),
                count = counts[EventType.DEFECTIVE],
                selected = selectedFilter == EventType.DEFECTIVE,
                accent = eventColor(EventType.DEFECTIVE),
                onClick = { onFilterSelected(EventType.DEFECTIVE) }
            )
            HistoryFilterChip(
                label = stringResource(R.string.filter_expired),
                count = counts[EventType.EXPIRED],
                selected = selectedFilter == EventType.EXPIRED,
                accent = eventColor(EventType.EXPIRED),
                onClick = { onFilterSelected(EventType.EXPIRED) }
            )
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = stringResource(R.string.history_empty_title),
                    description = stringResource(R.string.empty_history)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.xs,
                    bottom = Spacing.xxl
                )
            ) {
                groups.forEach { (dayLabel, dayEvents) ->
                    item(key = "header-$dayLabel", contentType = "header") {
                        DayHeader(label = dayLabel, count = dayEvents.size)
                    }
                    items(dayEvents, key = { it.id }) { event ->
                        TimelineRow(
                            event = event,
                            onClick = { onEventClicked(event.penId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(label: String, count: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.padding(vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = stringResource(R.string.history_events_count, PersianText.number(count)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TimelineRow(
    event: HistoryEvent,
    onClick: () -> Unit
) {
    val accent = eventColor(event.eventType)
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline dot
        Column(
            modifier = Modifier
                .width(22.dp)
                .padding(top = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = Spacing.sm)
        ) {
            AppCard(onClick = onClick) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.penName ?: stringResource(R.string.insulin_pen_default),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        event.penType?.let { type ->
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = type.persianLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = typeColor(type)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EventChip(eventType = event.eventType)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = DateUtils.displayDateTime(event.eventTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (event.description.isNotBlank()) {
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    count: Int?,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(if (selected) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (selected) accent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(Radius.pill)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = PersianText.number(count),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
