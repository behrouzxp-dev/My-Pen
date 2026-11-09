package com.mypen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mypen.app.R
import com.mypen.app.model.InsulinPen
import com.mypen.app.model.InsulinType
import com.mypen.app.model.PenStatus
import com.mypen.app.ui.components.AppCard
import com.mypen.app.ui.components.AppPill
import com.mypen.app.ui.components.EmptyState
import com.mypen.app.ui.components.PriorityIndicator
import com.mypen.app.ui.components.ShelfLifeBar
import com.mypen.app.ui.components.StatusChip
import com.mypen.app.ui.components.TypeChip
import com.mypen.app.ui.components.TypeSelector
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils
import com.mypen.app.util.PersianText

/** Sort strategies offered above the inventory list. */
enum class InventorySort {
    EXPIRY, NAME, NEWEST
}

@Composable
fun InventoryScreen(
    pens: List<InsulinPen>,
    currentType: InsulinType,
    today: String,
    onTypeSelected: (InsulinType) -> Unit,
    onPenSelected: (InsulinPen) -> Unit,
    onMarkOpened: (InsulinPen) -> Unit,
    onDeletePen: (InsulinPen) -> Unit,
    onAddPen: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(InventorySort.EXPIRY) }

    val visiblePens = remember(pens, query, sort) {
        val filtered = if (query.isBlank()) {
            pens
        } else {
            val needle = query.trim()
            pens.filter { pen -> pen.name.contains(needle, ignoreCase = true) }
        }
        when (sort) {
            InventorySort.EXPIRY -> filtered.sortedBy { it.expiryDate }
            InventorySort.NAME -> filtered.sortedBy { it.name }
            InventorySort.NEWEST -> filtered.sortedByDescending { it.entryDate }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Type switcher (segment control) + search + sort
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            TypeSelector(
                selected = currentType,
                onSelected = onTypeSelected
            )
            SearchField(
                query = query,
                onQueryChange = { query = it }
            )
            SortRow(
                sort = sort,
                onSortChange = { sort = it }
            )
        }

        if (visiblePens.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = if (query.isBlank()) Icons.Filled.Inventory2 else Icons.Filled.Search,
                    title = if (query.isBlank()) {
                        stringResource(R.string.inventory_empty_title)
                    } else {
                        stringResource(R.string.search_empty_title)
                    },
                    description = if (query.isBlank()) {
                        if (currentType == InsulinType.LONG_ACTING) {
                            stringResource(R.string.empty_inventory_long)
                        } else {
                            stringResource(R.string.empty_inventory_short)
                        }
                    } else {
                        stringResource(R.string.search_empty_desc)
                    },
                    actionLabel = if (query.isBlank()) stringResource(R.string.btn_add_pen) else null,
                    onAction = if (query.isBlank()) onAddPen else null
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.xxl, top = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(visiblePens, key = { it.id }) { pen ->
                    PenRowCard(
                        pen = pen,
                        today = today,
                        onClick = { onPenSelected(pen) },
                        onMarkOpened = { onMarkOpened(pen) },
                        onDelete = { onDeletePen(pen) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.md))
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.action_clear_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SortRow(sort: InventorySort, onSortChange: (InventorySort) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        SortChip(
            label = stringResource(R.string.sort_expiry),
            selected = sort == InventorySort.EXPIRY,
            onClick = { onSortChange(InventorySort.EXPIRY) },
            modifier = Modifier.weight(1f)
        )
        SortChip(
            label = stringResource(R.string.sort_name),
            selected = sort == InventorySort.NAME,
            onClick = { onSortChange(InventorySort.NAME) },
            modifier = Modifier.weight(1f)
        )
        SortChip(
            label = stringResource(R.string.sort_newest),
            selected = sort == InventorySort.NEWEST,
            onClick = { onSortChange(InventorySort.NEWEST) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(Radius.pill)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun PenRowCard(
    pen: InsulinPen,
    today: String,
    onClick: () -> Unit,
    onMarkOpened: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val daysRemaining = DateUtils.calculateDaysRemaining(pen.expiryDate, today)
    val accent = expiryAccent(daysRemaining)

    AppCard(onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.md, bottom = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = pen.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        TypeChip(type = pen.type)
                        StatusChip(status = pen.status)
                    }
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.action_more),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (pen.status == PenStatus.ACTIVE) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.btn_mark_opened)) },
                                leadingIcon = {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                },
                                onClick = {
                                    menuOpen = false
                                    onMarkOpened()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.btn_delete_permanently),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            ShelfLifeBar(
                fractionElapsed = DateUtils.shelfLifeProgress(pen.entryDate, pen.expiryDate, today),
                accent = accent
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DateUtils.remainingLabel(daysRemaining),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent
                    )
                    Text(
                        text = stringResource(
                            R.string.expiry_on_date,
                            DateUtils.displayDateLong(pen.expiryDate)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AppPill(
                    text = stringResource(R.string.priority_short, PersianText.number(pen.priority)),
                    accent = accent,
                    showDot = false
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                PriorityIndicator(priority = pen.priority, accent = accent)
            }
        }
    }
}
