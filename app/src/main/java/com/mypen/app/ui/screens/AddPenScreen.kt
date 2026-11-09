package com.mypen.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mypen.app.R
import com.mypen.app.model.InsulinType
import com.mypen.app.ui.components.AppCard
import com.mypen.app.ui.components.DateField
import com.mypen.app.ui.components.LocalAppMessages
import com.mypen.app.ui.components.MyPenTopBar
import com.mypen.app.ui.components.SectionHeader
import com.mypen.app.ui.components.TypeSelector
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPenScreen(
    onNavigateBack: () -> Unit,
    onSavePens: (
        name: String,
        type: InsulinType,
        expiry: String,
        count: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit
) {
    val messages = LocalAppMessages.current
    val errEmptyName = stringResource(R.string.err_empty_name)
    val errInvalidDate = stringResource(R.string.err_invalid_date)
    val errInvalidCount = stringResource(R.string.err_invalid_count)
    val errCountTooLarge = stringResource(R.string.err_count_too_large)
    val msgAdded = stringResource(R.string.msg_pens_added)

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InsulinType.LONG_ACTING) }
    var expiryDate by remember { mutableStateOf("") }
    var count by remember { mutableStateOf(1) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var expiryError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val isDirty = name.isNotBlank() || expiryDate.isNotBlank()

    fun attemptClose() {
        if (isDirty) showDiscardDialog = true else onNavigateBack()
    }

    Scaffold(
        topBar = {
            MyPenTopBar(
                title = stringResource(R.string.add_pen_title),
                onBack = { attemptClose() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            SectionHeader(title = stringResource(R.string.form_section_details))
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text(stringResource(R.string.field_insulin_name)) },
                        placeholder = { Text(stringResource(R.string.field_insulin_name_hint)) },
                        isError = nameError != null,
                        supportingText = {
                            nameError?.let { error -> Text(text = error) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Text(
                        text = stringResource(R.string.field_effect_type),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TypeSelector(
                        selected = selectedType,
                        onSelected = { selectedType = it }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Text(
                            text = stringResource(R.string.field_pen_count),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CountStepper(
                            count = count,
                            onCountChange = { count = it }
                        )
                        Text(
                            text = stringResource(R.string.field_pen_count_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SectionHeader(title = stringResource(R.string.form_section_expiry))
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    DateField(
                        label = stringResource(R.string.field_expiry_date),
                        isoDate = expiryDate,
                        onDateChange = {
                            expiryDate = it
                            expiryError = null
                        },
                        errorText = expiryError
                    )
                    Text(
                        text = stringResource(R.string.form_jalali_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    var hasError = false
                    if (name.trim().isEmpty()) {
                        nameError = errEmptyName
                        hasError = true
                    }
                    if (!DateUtils.isValidDate(expiryDate)) {
                        expiryError = errInvalidDate
                        hasError = true
                    }
                    if (count <= 0) {
                        messages.error(errInvalidCount)
                        hasError = true
                    } else if (count > 100) {
                        messages.error(errCountTooLarge)
                        hasError = true
                    }

                    if (!hasError && !isSaving) {
                        isSaving = true
                        onSavePens(
                            name.trim(),
                            selectedType,
                            expiryDate,
                            count,
                            {
                                isSaving = false
                                name = ""
                                expiryDate = ""
                                count = 1
                                nameError = null
                                expiryError = null
                                messages.success(msgAdded)
                            },
                            { message ->
                                isSaving = false
                                messages.error(message)
                            }
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(Radius.md)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = stringResource(R.string.btn_save_pens),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.dialog_discard_title)) },
            text = { Text(stringResource(R.string.dialog_discard_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.btn_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun CountStepper(count: Int, onCountChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        FilledTonalIconButton(
            onClick = { if (count > 1) onCountChange(count - 1) },
            enabled = count > 1
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.action_decrease),
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = com.mypen.app.util.PersianText.number(count),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        FilledTonalIconButton(
            onClick = { if (count < 100) onCountChange(count + 1) },
            enabled = count < 100
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.action_increase),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
