package com.arena.mypen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.arena.mypen.data.PenRepository
import com.arena.mypen.model.EventType
import com.arena.mypen.model.InsulinPen
import com.arena.mypen.model.InsulinType
import com.arena.mypen.notification.NotificationHelper
import com.arena.mypen.ui.screens.*
import com.arena.mypen.ui.theme.MyPenTheme
import com.arena.mypen.ui.theme.PrimaryBlue
import com.arena.mypen.ui.theme.SurfaceWhite
import com.arena.mypen.util.DateUtils

enum class NavigationTab {
    DASHBOARD,
    INVENTORY,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: PenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = PenRepository.getInstance(this)

        requestNotificationPermissionIfNeeded()

        setContent {
            MyPenTheme {
                AppContent(
                    repository = repository,
                    initialTab = parseInitialTab(intent),
                    initialType = parseInitialType(intent)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.checkAndSendNotifications(this)
    }

    private fun parseInitialTab(intent: Intent?): NavigationTab {
        return if (intent != null && intent.getStringExtra("OPEN_TAB") == "INVENTORY") {
            NavigationTab.INVENTORY
        } else {
            NavigationTab.DASHBOARD
        }
    }

    private fun parseInitialType(intent: Intent?): InsulinType {
        val typeStr = intent?.getStringExtra("PEN_TYPE")
        return InsulinType.fromString(typeStr)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    repository: PenRepository,
    initialTab: NavigationTab,
    initialType: InsulinType
) {
    var currentTab by remember { mutableStateOf(initialTab) }
    var inventoryType by remember { mutableStateOf(initialType) }

    // Navigation stacks for sub-screens
    var isAddPenOpen by remember { mutableStateOf(false) }
    var selectedPenId by remember { mutableStateOf<String?>(null) }
    var isExpiredPensOpen by remember { mutableStateOf(false) }

    // State triggering refresh across screens
    var dataVersion by remember { mutableStateOf(0) }

    val today = remember(dataVersion) { DateUtils.todayDate() }
    val metrics = remember(dataVersion) { repository.getDashboardMetrics(today) }
    val activePens = remember(dataVersion, inventoryType) { repository.getActivePens(inventoryType, today) }
    var historyFilter by remember { mutableStateOf<EventType?>(null) }
    val historyEvents = remember(dataVersion, historyFilter) { repository.getAllHistory(historyFilter) }
    val settings = remember(dataVersion) { repository.getNotificationSettings() }

    // Sub-screen 1: Add Pen
    if (isAddPenOpen) {
        AddPenScreen(
            onNavigateBack = { isAddPenOpen = false },
            onSavePens = { name, type, expiry, count, onSuccess ->
                try {
                    val ids = repository.addPens(name, type, expiry, count, DateUtils.todayDate())
                    dataVersion++
                    onSuccess()
                } catch (e: Exception) {
                    // Handled in screen
                }
            }
        )
        return
    }

    // Sub-screen 2: Pen Details
    if (selectedPenId != null) {
        val pen = remember(dataVersion, selectedPenId) { repository.getPenById(selectedPenId) }
        val penHistory = remember(dataVersion, selectedPenId) { repository.getPenHistory(selectedPenId) }

        PenDetailsScreen(
            pen = pen,
            historyEvents = penHistory,
            onNavigateBack = { selectedPenId = null },
            onUpdatePen = { newName, newType, newExpiry ->
                repository.updatePen(selectedPenId!!, newName, newType, newExpiry, DateUtils.todayDate())
                dataVersion++
            },
            onMarkOpened = { openedDate ->
                repository.markPenAsOpened(selectedPenId!!, openedDate, DateUtils.todayDate())
                dataVersion++
            },
            onMarkDefective = { reason ->
                repository.markPenAsDefective(selectedPenId!!, reason, DateUtils.todayDate())
                dataVersion++
            },
            onDeletePen = {
                repository.deletePenPermanently(selectedPenId!!)
                dataVersion++
                selectedPenId = null
            }
        )
        return
    }

    // Sub-screen 3: Expired Pens
    if (isExpiredPensOpen) {
        val expiredPens = remember(dataVersion) { repository.getExpiredPens() }
        ExpiredPensScreen(
            pens = expiredPens,
            onNavigateBack = { isExpiredPensOpen = false },
            onPenSelected = { pen -> selectedPenId = pen.id }
        )
        return
    }

    // Main Tabs Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            NavigationTab.DASHBOARD -> stringResource(R.string.app_name)
                            NavigationTab.INVENTORY -> stringResource(R.string.nav_inventory)
                            NavigationTab.HISTORY -> stringResource(R.string.nav_history)
                            NavigationTab.SETTINGS -> stringResource(R.string.nav_settings)
                        },
                        color = SurfaceWhite,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceWhite) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.DASHBOARD,
                    onClick = { currentTab = NavigationTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_dashboard)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.INVENTORY,
                    onClick = { currentTab = NavigationTab.INVENTORY },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_inventory)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.HISTORY,
                    onClick = { currentTab = NavigationTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_history)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.SETTINGS,
                    onClick = { currentTab = NavigationTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> {
                    DashboardScreen(
                        metrics = metrics,
                        onNavigateToInventory = { type ->
                            inventoryType = type
                            currentTab = NavigationTab.INVENTORY
                        },
                        onNavigateToExpired = { isExpiredPensOpen = true },
                        onNavigateToAddPen = { isAddPenOpen = true }
                    )
                }
                NavigationTab.INVENTORY -> {
                    InventoryScreen(
                        currentType = inventoryType,
                        pens = activePens,
                        onTypeSelected = { inventoryType = it },
                        onPenSelected = { selectedPenId = it.id },
                        onNavigateToAddPen = { isAddPenOpen = true }
                    )
                }
                NavigationTab.HISTORY -> {
                    HistoryScreen(
                        events = historyEvents,
                        selectedFilter = historyFilter,
                        onFilterSelected = { historyFilter = it },
                        onEventClicked = { selectedPenId = it }
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        settings = settings,
                        onSettingsChanged = { newSettings ->
                            repository.saveNotificationSettings(newSettings)
                            dataVersion++
                        },
                        onExportBackup = { repository.exportBackupJson() },
                        onRestoreBackup = { json ->
                            repository.restoreFromJson(json)
                            dataVersion++
                        }
                    )
                }
            }
        }
    }
}
