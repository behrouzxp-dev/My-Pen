package com.mypen.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mypen.app.R
import com.mypen.app.model.InsulinPen
import com.mypen.app.ui.components.AppCard
import com.mypen.app.ui.components.EmptyState
import com.mypen.app.ui.components.MyPenTopBar
import com.mypen.app.ui.components.StatusChip
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiredPensScreen(
    pens: List<InsulinPen>,
    today: String,
    onNavigateBack: () -> Unit,
    onPenSelected: (InsulinPen) -> Unit
) {
    Scaffold(
        topBar = {
            MyPenTopBar(
                title = stringResource(R.string.expired_pens_title),
                subtitle = if (pens.isEmpty()) null else stringResource(
                    R.string.expired_pens_count,
                    com.mypen.app.util.PersianText.number(pens.size)
                ),
                onBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (pens.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.EventAvailable,
                    title = stringResource(R.string.expired_empty_title),
                    description = stringResource(R.string.empty_expired_list),
                    accent = StatusActive
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.sm,
                    bottom = Spacing.xxl
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(pens, key = { it.id }) { pen ->
                    val days = DateUtils.calculateDaysRemaining(pen.expiryDate, today)
                    AppCard(onClick = { onPenSelected(pen) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                            ) {
                                Text(
                                    text = "${pen.name} (${pen.type.persianLabel})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(
                                        R.string.expiry_on_date,
                                        DateUtils.displayDateLong(pen.expiryDate)
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = DateUtils.remainingLabel(days),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusExpired
                                )
                            }
                            StatusChip(status = pen.status)
                        }
                    }
                }
            }
        }
    }
}
