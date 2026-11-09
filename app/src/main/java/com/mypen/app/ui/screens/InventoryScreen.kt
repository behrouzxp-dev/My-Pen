package com.mypen.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mypen.app.R
import com.mypen.app.model.InsulinPen
import com.mypen.app.model.InsulinType
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils

@Composable
fun InventoryScreen(
    currentType: InsulinType,
    pens: List<InsulinPen>,
    onTypeSelected: (InsulinType) -> Unit,
    onPenSelected: (InsulinPen) -> Unit,
    onNavigateToAddPen: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPen,
                containerColor = PrimaryBlue,
                contentColor = OnPrimaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add_pen))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = if (currentType == InsulinType.LONG_ACTING) 0 else 1,
                containerColor = SurfaceDark,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = currentType == InsulinType.LONG_ACTING,
                    onClick = { onTypeSelected(InsulinType.LONG_ACTING) },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_long_acting),
                            fontWeight = if (currentType == InsulinType.LONG_ACTING) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
                Tab(
                    selected = currentType == InsulinType.SHORT_ACTING,
                    onClick = { onTypeSelected(InsulinType.SHORT_ACTING) },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_short_acting),
                            fontWeight = if (currentType == InsulinType.SHORT_ACTING) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryLightBlue)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.col_name),
                    modifier = Modifier.weight(2.8f),
                    color = PrimaryDarkBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = stringResource(R.string.col_expiry),
                    modifier = Modifier.weight(2.4f),
                    color = PrimaryDarkBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.col_status),
                    modifier = Modifier.weight(1.6f),
                    color = PrimaryDarkBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.col_priority),
                    modifier = Modifier.weight(1.4f),
                    color = PrimaryDarkBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.col_days_left),
                    modifier = Modifier.weight(1.8f),
                    color = PrimaryDarkBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            Divider(color = DividerColor, thickness = 1.dp)

            // Content List or Empty State
            if (pens.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentType == InsulinType.LONG_ACTING)
                            stringResource(R.string.empty_inventory_long)
                        else
                            stringResource(R.string.empty_inventory_short),
                        color = TextHint,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(pens, key = { it.id }) { pen ->
                        InventoryRow(pen = pen, onClick = { onPenSelected(pen) })
                        Divider(color = DividerColor, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryRow(pen: InsulinPen, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Name
        Text(
            text = pen.name,
            modifier = Modifier.weight(2.8f),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start
        )

        // 2. Expiry Date
        Text(
            text = pen.expiryDate,
            modifier = Modifier.weight(2.4f),
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        // 3. Status Badge
        Box(
            modifier = Modifier.weight(1.6f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = StatusActiveBg,
                border = BorderStroke(1.dp, StatusActive)
            ) {
                Text(
                    text = pen.status.persianLabel,
                    color = StatusActive,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // 4. Priority Circle
        Box(
            modifier = Modifier.weight(1.4f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryLightBlue,
                border = BorderStroke(1.dp, PrimaryBlue),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = DateUtils.formatEnglish(pen.priority),
                        color = PrimaryDarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 5. Days Remaining
        Text(
            text = DateUtils.formatEnglish(pen.daysRemaining),
            modifier = Modifier.weight(1.8f),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
