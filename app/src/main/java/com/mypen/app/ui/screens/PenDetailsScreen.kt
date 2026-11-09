package com.mypen.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mypen.app.R
import com.mypen.app.model.*
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenDetailsScreen(
    pen: InsulinPen?,
    historyEvents: List<HistoryEvent>,
    onNavigateBack: () -> Unit,
    onUpdatePen: (newName: String, newType: InsulinType, newExpiry: String) -> Unit,
    onMarkOpened: (openedDate: String) -> Unit,
    onMarkDefective: (reason: String) -> Unit,
    onDeletePen: () -> Unit
) {
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showOpenDialog by remember { mutableStateOf(false) }
    var showDefectiveDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pen_details_title), color = OnPrimaryColor, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "بازگشت", tint = OnPrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { paddingValues ->
        if (pen == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "قلم مورد نظر یافت نشد.", color = TextHint)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Pen Header & Metadata
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, DividerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Name & Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pen.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (pen.type == InsulinType.LONG_ACTING) TypeLongActingBg else TypeShortActingBg,
                            border = BorderStroke(1.dp, if (pen.type == InsulinType.LONG_ACTING) TypeLongActing else TypeShortActing)
                        ) {
                            Text(
                                text = pen.type.persianLabel,
                                color = if (pen.type == InsulinType.LONG_ACTING) TypeLongActing else TypeShortActing,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (pen.status) {
                                PenStatus.ACTIVE -> StatusActiveBg
                                PenStatus.OPENED -> StatusOpenedBg
                                PenStatus.DEFECTIVE -> StatusDefectiveBg
                                PenStatus.EXPIRED -> StatusExpiredBg
                            },
                            border = BorderStroke(
                                1.dp,
                                when (pen.status) {
                                    PenStatus.ACTIVE -> StatusActive
                                    PenStatus.OPENED -> StatusOpened
                                    PenStatus.DEFECTIVE -> StatusDefective
                                    PenStatus.EXPIRED -> StatusExpired
                                }
                            )
                        ) {
                            Text(
                                text = pen.status.persianLabel,
                                color = when (pen.status) {
                                    PenStatus.ACTIVE -> StatusActive
                                    PenStatus.OPENED -> StatusOpened
                                    PenStatus.DEFECTIVE -> StatusDefective
                                    PenStatus.EXPIRED -> StatusExpired
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Divider(color = DividerColor)

                    DetailRow(label = stringResource(R.string.label_pen_id), value = pen.id)
                    DetailRow(label = stringResource(R.string.label_pen_expiry), value = pen.expiryDate, isBold = true)

                    if (pen.status == PenStatus.ACTIVE) {
                        DetailRow(
                            label = stringResource(R.string.label_days_remaining),
                            value = "${DateUtils.formatEnglish(pen.daysRemaining)} روز",
                            valueColor = PrimaryDarkBlue,
                            isBold = true
                        )
                    }

                    DetailRow(label = stringResource(R.string.label_entry_date), value = pen.entryDate)

                    if (!pen.openedDate.isNullOrBlank()) {
                        DetailRow(label = stringResource(R.string.label_opened_date), value = pen.openedDate!!, valueColor = StatusOpened, isBold = true)
                    }

                    if (!pen.defectiveDate.isNullOrBlank()) {
                        DetailRow(label = stringResource(R.string.label_defective_date), value = pen.defectiveDate!!, valueColor = StatusDefective)
                        DetailRow(label = stringResource(R.string.label_defective_reason), value = pen.defectiveReason ?: "-")
                    }
                }
            }

            // Card 2: Operations / Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, DividerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "عملیات روی این قلم",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    // Edit button
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.btn_edit_pen), fontSize = 13.sp)
                    }

                    // Mark Opened (active only)
                    if (pen.status == PenStatus.ACTIVE) {
                        Button(
                            onClick = { showOpenDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusOpened)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.btn_mark_opened), fontSize = 13.sp)
                        }

                        // Mark Defective (active only)
                        OutlinedButton(
                            onClick = { showDefectiveDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, StatusDefective),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDefective)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.btn_mark_defective), fontSize = 13.sp)
                        }
                    }

                    // Delete Permanently
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StatusExpired),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusExpired)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.btn_delete_permanently), fontSize = 13.sp)
                    }
                }
            }

            // Card 3: History Events
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, DividerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_pen_history),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    if (historyEvents.isEmpty()) {
                        Text(
                            text = "هنوز رویدادی ثبت نشده است.",
                            color = TextHint,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        historyEvents.forEach { ev ->
                            HistoryItem(event = ev)
                            Divider(color = DividerColor, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }

    // Dialog 1: Edit
    if (showEditDialog) {
        var editName by remember { mutableStateOf(pen?.name ?: "") }
        var editType by remember { mutableStateOf(pen?.type ?: InsulinType.LONG_ACTING) }
        var editExpiry by remember { mutableStateOf(pen?.expiryDate ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.dialog_edit_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.field_insulin_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RadioButton(
                                selected = editType == InsulinType.LONG_ACTING,
                                onClick = { editType = InsulinType.LONG_ACTING },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Text(stringResource(R.string.type_long), fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RadioButton(
                                selected = editType == InsulinType.SHORT_ACTING,
                                onClick = { editType = InsulinType.SHORT_ACTING },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Text(stringResource(R.string.type_short), fontSize = 13.sp)
                        }
                    }

                    OutlinedTextField(
                        value = editExpiry,
                        onValueChange = { editExpiry = it },
                        label = { Text(stringResource(R.string.field_expiry_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank() && DateUtils.isValidDate(editExpiry)) {
                            onUpdatePen(editName.trim(), editType, editExpiry.trim())
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
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

    // Dialog 2: Mark Opened
    if (showOpenDialog) {
        var openDate by remember { mutableStateOf(DateUtils.todayDate()) }

        AlertDialog(
            onDismissRequest = { showOpenDialog = false },
            title = { Text(stringResource(R.string.dialog_open_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.dialog_open_prompt), fontSize = 13.sp)
                    OutlinedTextField(
                        value = openDate,
                        onValueChange = { openDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (DateUtils.isValidDate(openDate)) {
                            onMarkOpened(openDate.trim())
                            showOpenDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOpened)
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

    // Dialog 3: Mark Defective
    if (showDefectiveDialog) {
        var defectReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDefectiveDialog = false },
            title = { Text(stringResource(R.string.dialog_defective_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.dialog_defective_prompt), fontSize = 13.sp)
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
                Button(
                    onClick = {
                        onMarkDefective(defectReason.trim())
                        showDefectiveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDefective)
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

    // Dialog 4: Delete Confirm
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title), fontWeight = FontWeight.Bold, color = StatusExpired) },
            text = { Text(stringResource(R.string.dialog_delete_message), fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeletePen()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusExpired)
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
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HistoryItem(event: HistoryEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.eventType.persianLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = when (event.eventType) {
                    EventType.ENTRY -> StatusActive
                    EventType.OPENED -> StatusOpened
                    EventType.DEFECTIVE -> StatusDefective
                    EventType.EXPIRED -> StatusExpired
                    EventType.EDITED -> TypeLongActing
                }
            )
            Text(text = event.eventTime, color = TextHint, fontSize = 10.sp)
        }
        Text(text = event.description, color = TextSecondary, fontSize = 12.sp)
    }
}
