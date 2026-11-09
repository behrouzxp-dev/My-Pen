package com.mypen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mypen.app.R
import com.mypen.app.ui.theme.Radius
import com.mypen.app.ui.theme.Spacing
import com.mypen.app.util.DateUtils

/**
 * Read-only date field that opens the Jalali picker. It shows the Jalali
 * representation while the caller keeps working with an ISO Gregorian date.
 */
@Composable
fun DateField(
    label: String,
    isoDate: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null
) {
    var pickerOpen by remember { mutableStateOf(false) }
    val hasValue = isoDate.isNotBlank()
    val borderColor = if (errorText != null) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.sm))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, borderColor, RoundedCornerShape(Radius.sm))
                .clickable { pickerOpen = true }
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (hasValue) {
                        DateUtils.displayDateLong(isoDate)
                    } else {
                        stringResource(R.string.field_date_placeholder)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasValue) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = stringResource(R.string.action_pick_date),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = Spacing.lg, top = Spacing.xs)
            )
        }
    }

    if (pickerOpen) {
        JalaliDatePickerDialog(
            title = label,
            initialIsoDate = if (hasValue) isoDate else null,
            onDismiss = { pickerOpen = false },
            onDateSelected = { selected ->
                pickerOpen = false
                onDateChange(selected)
            }
        )
    }
}
