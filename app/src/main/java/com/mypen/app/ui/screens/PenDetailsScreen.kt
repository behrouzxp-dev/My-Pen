package com.mypen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mypen.app.R
import com.mypen.app.model.EventType
import com.mypen.app.model.HistoryEvent
import com.mypen.app.model.InsulinPen
import com.mypen.app.model.InsulinType
import com.mypen.app.model.PenStatus
import com.mypen.app.ui.components.AppCard
import com.mypen.app.ui.components.CountdownRing
import com.mypen.app.ui.components.DateField
import com.mypen.app.ui.components.EventChip
import com.mypen.app.ui.components.MyPenTopBar
import com.mypen.app.ui.components.SectionHeader
import com.mypen.app.ui.components.ShelfLifeBar
import com.mypen.app.ui.components.StatusChip
import com.mypen.app.ui.components.TypeSelector
import com.mypen.app.ui.components.TypeChip
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils
import com.mypen.app.util.PersianText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenDetailsScreen(
    pen: InsulinPen?,
    historyEvents: List<HistoryEvent>,
    today: String,
    isLoading: Boolean = false,
    onNavigateBack: () -> Unit,
    onUpdatePen: (newName: String, newType: InsulinType, newExpiry: String) -> Unit,
    onMarkOpened: (openedDate: String) -> Unit,
    onMarkDefective: (reason: String) -> Unit,
    onDeletePen: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showOpenDialog by remember { mutableStateOf(false) }
    var showDefectiveDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MyPenTopBar(
                title = stringResource(R.string.pen_details_title),
                onBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (pen == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    com.mypen.app.ui.components.EmptyState(
                        icon = Icons.Filled.Search,
                        title = stringResource(R.string.pen_not_found_title),
                        description = stringResource(R.string.pen_not_found_desc),
                        actionLabel = stringResource(R.string.action_back),
                        onAction = onNavigateBack
                    )
                }
            }
            return@Scaffold
        }

        val daysRemaining = DateUtils.calculateDaysRemaining(pen.expiryDate, today)
        val accent = expiryAccent(daysRemaining)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // 1. Hero card
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CountdownRing(
                            fractionRemaining = (daysRemaining.toFloat() / 90f).coerceIn(0f, 1f),
                            accent = accent,
                            size = 92.dp,
                            strokeWidth = 7.dp
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = PersianText.number(if (daysRemaining > 0L) daysRemaining else 0L),
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
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                TypeChip(type = pen.type)
                                StatusChip(status = pen.status)
                            }
                            Text(
                                text = DateUtils.remainingLabel(daysRemaining),
                                style = MaterialTheme.typography.labelMedium,
                                color = accent
                            )
                        }
                    }

                    ShelfLifeBar(
                        fractionElapsed = DateUtils.shelfLifeProgress(
                            pen.entryDate,
                            pen.expiryDate,
                            today
                        ),
                        accent = accent
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.label_pen_expiry),
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

            // 2. Metadata
            SectionHeader(title = stringResource(R.string.section_pen_information))
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    DetailRow(
                        label = stringResource(R.string.label_pen_id),
                        value = pen.id.take(8)
                    )
                    DetailRow(
                        label = stringResource(R.string.label_entry_date),
                        value = DateUtils.displayDateLong(pen.entryDate)
                    )
                    if (!pen.openedDate.isNullOrBlank()) {
                        DetailRow(
                            label = stringResource(R.string.label_opened_date),
                            value = DateUtils.displayDateLong(pen.openedDate),
                            valueColor = StatusOpened
                        )
                    }
                    if (!pen.defectiveDate.isNullOrBlank()) {
                        DetailRow(
                            label = stringResource(R.string.label_defective_date),
                            value = DateUtils.displayDateLong(pen.defectiveDate),
                            valueColor = StatusDefective
                        )
                        DetailRow(
                            label = stringResource(R.string.label_defective_reason),
                            value = pen.defectiveReason ?: stringResource(R.string.value_not_set)
                        )
                    }
                }
            }

            // 3. Actions
            SectionHeader(title = stringResource(R.string.section_pen_actions))
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(Radius.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = stringResource(R.string.btn_edit_pen),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    if (pen.status == PenStatus.ACTIVE) {
                        Button(
                            onClick = { showOpenDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(Radius.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = stringResource(R.string.btn_mark_opened),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        OutlinedButton(
                            onClick = { showDefectiveDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(Radius.sm),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDefective)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = stringResource(R.string.btn_mark_defective),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    TextButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = stringResource(R.string.btn_delete_permanently),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // 4. History
            SectionHeader(title = stringResource(R.string.label_pen_history))
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (historyEvents.isEmpty()) {
                        Text(
                            text = stringResource(R.string.pen_history_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        historyEvents.forEachIndexed { index, event ->
                            HistoryItem(event = event)
                            if (index != historyEvents.lastIndex) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }

    // Dialog 1: edit
    if (showEditDialog && pen != null) {
        var editName by remember { mutableStateOf(pen.name) }
        var editType by remember { mutableStateOf(pen.type) }
        var editExpiry by remember { mutableStateOf(pen.expiryDate) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.dialog_edit_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.field_insulin_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    TypeSelector(
                        selected = editType,
                        onSelected = { editType = it }
                    )
                    DateField(
                        label = stringResource(R.string.field_expiry_date),
                        isoDate = editExpiry,
                        onDateChange = { editExpiry = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editName.isNotBlank() && DateUtils.isValidDate(editExpiry)) {
                            onUpdatePen(editName.trim(), editType, editExpiry)
                            showEditDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_save_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Dialog 2: mark opened
    if (showOpenDialog) {
        var openDate by remember { mutableStateOf(DateUtils.todayDate()) }

        AlertDialog(
            onDismissRequest = { showOpenDialog = false },
            title = { Text(stringResource(R.string.dialog_open_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(
                        text = stringResource(R.string.dialog_open_prompt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DateField(
                        label = stringResource(R.string.dialog_open_date_label),
                        isoDate = openDate,
                        onDateChange = { openDate = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (DateUtils.isValidDate(openDate)) {
                            onMarkOpened(openDate)
                            showOpenDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm_open))
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Dialog 3: mark defective
    if (showDefectiveDialog) {
        var defectReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDefectiveDialog = false },
            title = { Text(stringResource(R.string.dialog_defective_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(
                        text = stringResource(R.string.dialog_defective_prompt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = defectReason,
                        onValueChange = { defectReason = it },
                        placeholder = { Text(stringResource(R.string.dialog_defective_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onMarkDefective(defectReason.trim())
                        showDefectiveDialog = false
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm_defective))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDefectiveDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Dialog 4: delete confirmation
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_title),
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_delete_message),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeletePen()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_confirm_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun HistoryItem(event: HistoryEvent) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EventChip(eventType = event.eventType)
            Text(
                text = DateUtils.displayDateTime(event.eventTime),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = event.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
