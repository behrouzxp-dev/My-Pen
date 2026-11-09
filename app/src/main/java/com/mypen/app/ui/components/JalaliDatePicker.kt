package com.mypen.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mypen.app.R
import com.mypen.app.ui.theme.Radius
import com.mypen.app.ui.theme.Spacing
import com.mypen.app.util.DateUtils
import com.mypen.app.util.JalaliDate
import com.mypen.app.util.PersianText

/**
 * Fully custom Jalali (Solar Hijri) date picker.
 *
 * The platform `DatePickerDialog` and the Material 3 date picker both render a
 * Gregorian calendar, which is not what Iranian users expect and which also
 * ignored the application theme. This picker is Persian-only, follows the app
 * theme and returns an ISO Gregorian string so storage stays unchanged.
 */
@Composable
fun JalaliDatePickerDialog(
    title: String,
    initialIsoDate: String?,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val todayIso = remember { DateUtils.todayDate() }
    val todayJalali = remember(todayIso) {
        JalaliDate.fromIsoDate(todayIso) ?: Triple(1400, 1, 1)
    }
    val initial = remember(initialIsoDate) {
        JalaliDate.fromIsoDate(initialIsoDate) ?: todayJalali
    }

    var year by remember { mutableStateOf(initial.first) }
    var month by remember { mutableStateOf(initial.second) }
    var selectedDay by remember { mutableStateOf(initial.third) }

    val totalDays = JalaliDate.daysInMonth(year, month)
    val firstWeekDay = JalaliDate.weekDayIndex(year, month, 1)
    val rowCount = ((firstWeekDay + totalDays) + 6) / 7

    fun selectMonth(delta: Int) {
        val newMonth = month + delta
        when {
            newMonth < 1 -> {
                month = 12
                year -= 1
            }
            newMonth > 12 -> {
                month = 1
                year += 1
            }
            else -> month = newMonth
        }
        selectedDay = selectedDay.coerceAtMost(JalaliDate.daysInMonth(year, month))
    }

    fun selectYear(delta: Int) {
        year += delta
        selectedDay = selectedDay.coerceAtMost(JalaliDate.daysInMonth(year, month))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            shape = RoundedCornerShape(Radius.xl),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Year selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectYear(-1) }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = stringResource(R.string.date_picker_previous_year),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${PersianText.number(year)}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { selectYear(1) }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = stringResource(R.string.date_picker_next_year),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Month selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectMonth(-1) }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = stringResource(R.string.date_picker_previous_month),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = JalaliDate.monthNames.getOrElse(month - 1) { "" },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { selectMonth(1) }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = stringResource(R.string.date_picker_next_month),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Weekday header (Saturday first)
                Row(modifier = Modifier.fillMaxWidth()) {
                    JalaliDate.weekDayNames.forEach { dayName ->
                        Text(
                            text = dayName.take(1),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Day grid
                Column(modifier = Modifier.fillMaxWidth()) {
                    for (rowIndex in 0 until rowCount) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (columnIndex in 0 until 7) {
                                val cellIndex = rowIndex * 7 + columnIndex
                                val day = cellIndex - firstWeekDay + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day in 1..totalDays) {
                                        val isSelected = day == selectedDay
                                        val isToday = year == todayJalali.first &&
                                            month == todayJalali.second &&
                                            day == todayJalali.third
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                                        else -> MaterialTheme.colorScheme.surface
                                                    }
                                                )
                                                .clickable { selectedDay = day },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = PersianText.number(day),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            year = todayJalali.first
                            month = todayJalali.second
                            selectedDay = todayJalali.third
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Today,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = stringResource(R.string.date_picker_today),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.btn_cancel),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Button(
                        onClick = {
                            onDateSelected(JalaliDate.toIsoDate(year, month, selectedDay))
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.btn_confirm_date),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
