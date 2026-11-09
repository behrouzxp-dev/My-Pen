package com.mypen.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.mypen.app.data.PenRepository
import com.mypen.app.model.DashboardMetrics
import com.mypen.app.model.EventType
import com.mypen.app.model.HistoryEvent
import com.mypen.app.model.InsulinPen
import com.mypen.app.model.InsulinType
import com.mypen.app.model.NotificationSettings
import com.mypen.app.notification.NotificationHelper
import com.mypen.app.ui.screens.*
import com.mypen.app.ui.theme.MyPenTheme
import com.mypen.app.ui.theme.OnPrimaryColor
import com.mypen.app.ui.theme.PrimaryBlue
import com.mypen.app.ui.theme.SurfaceDark
import com.mypen.app.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class NavigationTab {
    DASHBOARD,
    INVENTORY,
    HISTORY,
    SETTINGS
}

/**
 * Immutable snapshot of everything the main tabs need, loaded off the main
 * thread in a single background pass.
 */
private data class AppUiData(
    val today: String,
    val metrics: DashboardMetrics,
    val activePens: List<InsulinPen>,
    val historyEvents: List<HistoryEvent>,
    val settings: NotificationSettings
)

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
        // Never touch the database or post notifications on the UI thread.
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { NotificationHelper.checkAndSendNotifications(this@MainActivity) }
        }
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(initialTab) }
    var inventoryType by remember { mutableStateOf(initialType) }

    // Navigation stacks for sub-screens
    var isAddPenOpen by remember { mutableStateOf(false) }
    var selectedPenId by remember { mutableStateOf<String?>(null) }
    var isExpiredPensOpen by remember { mutableStateOf(false) }

    // State triggering refresh across screens
    var dataVersion by remember { mutableStateOf(0) }
    var historyFilter by remember { mutableStateOf<EventType?>(null) }

    // Load all tab data off the main thread. Recomposes only when inputs change.
    val data by produceState<AppUiData?>(
        initialValue = null,
        dataVersion,
        inventoryType,
        historyFilter
    ) {
        value = withContext(Dispatchers.IO) {
            val today = DateUtils.todayDate()
            AppUiData(
                today = today,
                metrics = repository.getDashboardMetrics(today),
                activePens = repository.getActivePens(inventoryType, today),
                historyEvents = repository.getAllHistory(historyFilter),
                settings = repository.getNotificationSettings()
            )
        }
    }

    // Sub-screen 1: Add Pen
    if (isAddPenOpen) {
        AddPenScreen(
            onNavigateBack = { isAddPenOpen = false },
            onSavePens = { name, type, expiry, count, onSuccess, onError ->
                scope.launch(Dispatchers.IO) {
                    val result = runCatching {
                        repository.addPens(name, type, expiry, count, DateUtils.todayDate())
                    }
                    if (result.isSuccess) dataVersion++
                    withContext(Dispatchers.Main) {
                        result.fold(
                            onSuccess = { onSuccess() },
                            onFailure = { onError(it.message ?: "خطا در ثبت قلم‌ها") }
                        )
                    }
                }
            }
        )
        return
    }

    // Sub-screen 2: Pen Details
    if (selectedPenId != null) {
        val pen by produceState<InsulinPen?>(initialValue = null, dataVersion, selectedPenId) {
            value = withContext(Dispatchers.IO) { repository.getPenById(selectedPenId) }
        }
        val penHistory by produceState(initialValue = emptyList<HistoryEvent>(), dataVersion, selectedPenId) {
            value = withContext(Dispatchers.IO) { repository.getPenHistory(selectedPenId) }
        }

        PenDetailsScreen(
            pen = pen,
            historyEvents = penHistory,
            onNavigateBack = { selectedPenId = null },
            onUpdatePen = { newName, newType, newExpiry ->
                scope.launch(Dispatchers.IO) {
                    val result = runCatching {
                        repository.updatePen(selectedPenId ?: return@launch, newName, newType, newExpiry, DateUtils.todayDate())
                    }
                    if (result.isSuccess) {
                        dataVersion++
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "خطا در ذخیره ویرایش: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onMarkOpened = { openedDate ->
                scope.launch(Dispatchers.IO) {
                    val result = runCatching {
                        repository.markPenAsOpened(selectedPenId ?: return@launch, openedDate, DateUtils.todayDate())
                    }
                    if (result.isSuccess) {
                        dataVersion++
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "خطا در ثبت باز شدن: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onMarkDefective = { reason ->
                scope.launch(Dispatchers.IO) {
                    val result = runCatching {
                        repository.markPenAsDefective(selectedPenId ?: return@launch, reason, DateUtils.todayDate())
                    }
                    if (result.isSuccess) {
                        dataVersion++
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "خطا در ثبت خرابی: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onDeletePen = {
                scope.launch(Dispatchers.IO) {
                    val result = runCatching {
                        repository.deletePenPermanently(selectedPenId ?: return@launch)
                    }
                    if (result.isSuccess) {
                        dataVersion++
                        withContext(Dispatchers.Main) { selectedPenId = null }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "خطا در حذف قلم: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
        return
    }

    // Sub-screen 3: Expired Pens
    if (isExpiredPensOpen) {
        val expiredPens by produceState(initialValue = emptyList<InsulinPen>(), dataVersion) {
            value = withContext(Dispatchers.IO) { repository.getExpiredPens() }
        }
        ExpiredPensScreen(
            pens = expiredPens,
            onNavigateBack = { isExpiredPensOpen = false },
            onPenSelected = { pen -> selectedPenId = pen.id }
        )
        return
    }

    // Main Tabs — wait for the first background load to finish.
    val uiData = data
    if (uiData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
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
                        color = OnPrimaryColor,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceDark) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.DASHBOARD,
                    onClick = { currentTab = NavigationTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_dashboard)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.18f)
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
                        indicatorColor = PrimaryBlue.copy(alpha = 0.18f)
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
                        indicatorColor = PrimaryBlue.copy(alpha = 0.18f)
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
                        indicatorColor = PrimaryBlue.copy(alpha = 0.18f)
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
                        metrics = uiData.metrics,
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
                        pens = uiData.activePens,
                        onTypeSelected = { inventoryType = it },
                        onPenSelected = { selectedPenId = it.id },
                        onNavigateToAddPen = { isAddPenOpen = true }
                    )
                }
                NavigationTab.HISTORY -> {
                    HistoryScreen(
                        events = uiData.historyEvents,
                        selectedFilter = historyFilter,
                        onFilterSelected = { historyFilter = it },
                        onEventClicked = { selectedPenId = it }
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        settings = uiData.settings,
                        onSettingsChanged = { newSettings ->
                            scope.launch(Dispatchers.IO) {
                                repository.saveNotificationSettings(newSettings)
                                dataVersion++
                            }
                        },
                        onExportBackup = { repository.exportBackupJson() },
                        onRestoreBackup = { json ->
                            // Intentionally synchronous: SettingsScreen relies on
                            // exceptions to show a failure toast, and restore is
                            // a rare, user-confirmed operation.
                            repository.restoreFromJson(json)
                            dataVersion++
                        }
                    )
                }
            }
        }
    }
}
