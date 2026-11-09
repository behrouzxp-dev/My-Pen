package com.mypen.app.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mypen.app.R
import com.mypen.app.model.InsulinType
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPenScreen(
    onNavigateBack: () -> Unit,
    onSavePens: (name: String, type: InsulinType, expiry: String, count: Int, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val errEmptyName = stringResource(R.string.err_empty_name)
    val errInvalidDate = stringResource(R.string.err_invalid_date)
    val errInvalidCount = stringResource(R.string.err_invalid_count)
    val errCountTooLarge = stringResource(R.string.err_count_too_large)

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InsulinType.LONG_ACTING) }
    var expiryDate by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("1") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var expiryError by remember { mutableStateOf<String?>(null) }
    var countError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.add_pen_title), color = SurfaceWhite, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "بازگشت", tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, DividerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Field 1: Insulin Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text(stringResource(R.string.field_insulin_name)) },
                        placeholder = { Text(stringResource(R.string.field_insulin_name_hint)) },
                        isError = nameError != null,
                        supportingText = { nameError?.let { Text(it, color = StatusExpired) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Field 2: Effect Type
                    Text(
                        text = stringResource(R.string.field_effect_type),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = selectedType == InsulinType.LONG_ACTING,
                                onClick = { selectedType = InsulinType.LONG_ACTING },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.type_long), fontSize = 14.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = selectedType == InsulinType.SHORT_ACTING,
                                onClick = { selectedType = InsulinType.SHORT_ACTING },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.type_short), fontSize = 14.sp)
                        }
                    }

                    // Field 3: Expiry Date
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = {
                            expiryDate = it
                            expiryError = null
                        },
                        label = { Text(stringResource(R.string.field_expiry_date)) },
                        placeholder = { Text(stringResource(R.string.field_expiry_date_hint)) },
                        isError = expiryError != null,
                        supportingText = { expiryError?.let { Text(it, color = StatusExpired) } },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                val cal = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
                                if (DateUtils.isValidDate(expiryDate)) {
                                    val parts = expiryDate.split("-")
                                    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                                }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        expiryDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                        expiryError = null
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "انتخاب تاریخ", tint = PrimaryBlue)
                            }
                        },
                        singleLine = true
                    )

                    // Field 4: Pen Count
                    OutlinedTextField(
                        value = countStr,
                        onValueChange = {
                            countStr = it
                            countError = null
                        },
                        label = { Text(stringResource(R.string.field_pen_count)) },
                        placeholder = { Text(stringResource(R.string.field_pen_count_hint)) },
                        isError = countError != null,
                        supportingText = { countError?.let { Text(it, color = StatusExpired) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Button
                    Button(
                        onClick = {
                            var hasError = false
                            if (name.trim().isEmpty()) {
                                nameError = errEmptyName
                                hasError = true
                            }
                            if (!DateUtils.isValidDate(expiryDate.trim())) {
                                expiryError = errInvalidDate
                                hasError = true
                            }
                            val count = countStr.toIntOrNull()
                            if (count == null || count <= 0) {
                                countError = errInvalidCount
                                hasError = true
                            } else if (count > 100) {
                                countError = errCountTooLarge
                                hasError = true
                            }

                            if (!hasError && !isSaving && count != null) {
                                isSaving = true
                                onSavePens(
                                    name.trim(),
                                    selectedType,
                                    expiryDate.trim(),
                                    count,
                                    {
                                        isSaving = false
                                        // Reset form for next entry
                                        name = ""
                                        expiryDate = ""
                                        countStr = "1"
                                        nameError = null
                                        expiryError = null
                                        countError = null
                                    },
                                    { message ->
                                        isSaving = false
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(22.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.btn_save_pens), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
