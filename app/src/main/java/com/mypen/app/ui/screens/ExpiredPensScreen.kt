package com.mypen.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.mypen.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiredPensScreen(
    pens: List<InsulinPen>,
    onNavigateBack: () -> Unit,
    onPenSelected: (InsulinPen) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.expired_pens_title), color = SurfaceWhite, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "بازگشت", tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StatusExpired)
            )
        }
    ) { paddingValues ->
        if (pens.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_expired_list),
                    color = TextHint,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pens, key = { it.id }) { pen ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPenSelected(pen) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, DividerColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${pen.name} (${pen.type.persianLabel})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "تاریخ انقضا: ${pen.expiryDate}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusExpiredBg,
                                border = BorderStroke(1.dp, StatusExpired)
                            ) {
                                Text(
                                    text = stringResource(R.string.status_expired_label),
                                    color = StatusExpired,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
