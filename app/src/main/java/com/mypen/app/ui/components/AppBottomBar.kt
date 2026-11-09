package com.mypen.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mypen.app.R
import com.mypen.app.util.PersianText

/** Tab identifiers shared with the navigation graph. */
object Tabs {
    const val DASHBOARD = "dashboard"
    const val INVENTORY = "inventory"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    val routes = listOf(DASHBOARD, INVENTORY, HISTORY, SETTINGS)
}

@Composable
fun MyPenBottomBar(
    currentRoute: String,
    expiringCount: Int,
    onNavigate: (String) -> Unit
) {
    val items: List<Pair<String, Pair<ImageVector, Int>>> = listOf(
        Tabs.DASHBOARD to (Icons.Filled.Dashboard to R.string.nav_dashboard),
        Tabs.INVENTORY to (Icons.Filled.Inventory2 to R.string.nav_inventory),
        Tabs.HISTORY to (Icons.Filled.History to R.string.nav_history),
        Tabs.SETTINGS to (Icons.Filled.Settings to R.string.nav_settings)
    )

    androidx.compose.foundation.layout.Column {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            items.forEach { (route, content) ->
                val icon = content.first
                val labelRes = content.second
                NavigationBarItem(
                    selected = currentRoute == route,
                    onClick = { onNavigate(route) },
                    icon = {
                        if (route == Tabs.INVENTORY && expiringCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = PersianText.number(expiringCount),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
