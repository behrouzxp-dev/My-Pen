package com.mypen.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mypen.app.model.EventType
import com.mypen.app.model.HistoryEvent
import com.mypen.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    events: List<HistoryEvent>,
    selectedFilter: EventType?,
    onFilterSelected: (EventType?) -> Unit,
    onEventClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text(stringResource(R.string.filter_all)) }
            )
            FilterChip(
                selected = selectedFilter == EventType.OPENED,
                onClick = { onFilterSelected(EventType.OPENED) },
                label = { Text(stringResource(R.string.filter_opened)) }
            )
            FilterChip(
                selected = selectedFilter == EventType.DEFECTIVE,
                onClick = { onFilterSelected(EventType.DEFECTIVE) },
                label = { Text(stringResource(R.string.filter_defective)) }
            )
            FilterChip(
                selected = selectedFilter == EventType.EXPIRED,
                onClick = { onFilterSelected(EventType.EXPIRED) },
                label = { Text(stringResource(R.string.filter_expired)) }
            )
        }

        Divider(color = DividerColor)

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_history),
                    color = TextHint,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEventClicked(event.penId) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, DividerColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = (event.penName ?: "قلم انسولین") +
                                            if (event.penType != null) " (${event.penType!!.persianLabel})" else "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (event.eventType) {
                                        EventType.ENTRY -> StatusActiveBg
                                        EventType.OPENED -> StatusOpenedBg
                                        EventType.DEFECTIVE -> StatusDefectiveBg
                                        EventType.EXPIRED -> StatusExpiredBg
                                        EventType.EDITED -> TypeLongActingBg
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when (event.eventType) {
                                            EventType.ENTRY -> StatusActive
                                            EventType.OPENED -> StatusOpened
                                            EventType.DEFECTIVE -> StatusDefective
                                            EventType.EXPIRED -> StatusExpired
                                            EventType.EDITED -> TypeLongActing
                                        }
                                    )
                                ) {
                                    Text(
                                        text = event.eventType.persianLabel,
                                        color = when (event.eventType) {
                                            EventType.ENTRY -> StatusActive
                                            EventType.OPENED -> StatusOpened
                                            EventType.DEFECTIVE -> StatusDefective
                                            EventType.EXPIRED -> StatusExpired
                                            EventType.EDITED -> TypeLongActing
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = event.eventTime,
                                color = TextHint,
                                fontSize = 11.sp
                            )

                            Text(
                                text = event.description,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
