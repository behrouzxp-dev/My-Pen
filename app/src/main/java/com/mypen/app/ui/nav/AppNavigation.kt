package com.mypen.app.ui.nav

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mypen.app.R
import com.mypen.app.data.PenRepository
import com.mypen.app.model.DashboardMetrics
import com.mypen.app.model.EventType
import com.mypen.app.model.HistoryEvent
import com.mypen.app.model.InsulinPen
import com.mypen.app.model.InsulinType
import com.mypen.app.model.NotificationSettings
import com.mypen.app.ui.components.AppSnackbarHost
import com.mypen.app.ui.components.LocalAppMessages
import com.mypen.app.ui.components.LoadingSkeleton
import com.mypen.app.ui.components.MyPenBottomBar
import com.mypen.app.ui.components.MyPenLargeTopBar
import com.mypen.app.ui.components.MyPenTopBar
import com.mypen.app.ui.components.Tabs
import com.mypen.app.ui.components.rememberAppMessages
import com.mypen.app.ui.screens.AddPenScreen
import com.mypen.app.ui.screens.DashboardScreen
import com.mypen.app.ui.screens.ExpiredPensScreen
import com.mypen.app.ui.screens.HistoryScreen
import com.mypen.app.ui.screens.InventoryScreen
import com.mypen.app.ui.screens.PenDetailsScreen
import com.mypen.app.ui.screens.SettingsScreen
import com.mypen.app.ui.theme.Motion
import com.mypen.app.util.DateUtils
import com.mypen.app.util.PersianText
import com.mypen.app.widget.MyPenWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Navigation routes of the application. */
object Routes {
    const val DASHBOARD = Tabs.DASHBOARD
    const val INVENTORY = Tabs.INVENTORY
    const val HISTORY = Tabs.HISTORY
    const val SETTINGS = Tabs.SETTINGS
    const val ADD_PEN = "add_pen"
    const val EXPIRED = "expired"
    const val PEN_DETAILS = "pen/{penId}"
    const val ARG_PEN_ID = "penId"

    fun penDetails(penId: String) = "pen/$penId"
}

/** Immutable snapshot of everything the tabs need, loaded in one IO pass. */
data class AppUiData(
    val today: String,
    val metrics: DashboardMetrics,
    val activePens: List<InsulinPen>,
    val historyEvents: List<HistoryEvent>,
    val settings: NotificationSettings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPenApp(
    repository: PenRepository,
    initialTab: String,
    initialType: InsulinType,
    initialPenId: String?
) {
    val context = LocalContext.current
    val messages = rememberAppMessages()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var dataVersion by remember { mutableStateOf(0) }
    var inventoryType by remember { mutableStateOf(initialType) }
    var historyFilter by remember { mutableStateOf<EventType?>(null) }

    val data by produceState<AppUiData?>(
        initialValue = null,
        dataVersion,
        inventoryType,
        historyFilter
    ) {
        value = withContext(Dispatchers.IO) {
            val today = DateUtils.todayDate()
            val settings = repository.getNotificationSettings()
            AppUiData(
                today = today,
                metrics = repository.getDashboardMetrics(today),
                activePens = repository.getActivePens(inventoryType, today),
                historyEvents = repository.getAllHistory(historyFilter),
                settings = settings
            )
        }
    }

    val snapshot = data
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.DASHBOARD
    val isTab = currentRoute in Tabs.routes

    fun refreshAll() {
        dataVersion++
        scope.launch(Dispatchers.IO) { runCatching { MyPenWidgetProvider.updateAll(context) } }
    }

    LaunchedEffect(initialPenId, initialTab) {
        if (initialPenId != null) {
            navController.navigate(Routes.penDetails(initialPenId))
        } else if (initialTab == Tabs.INVENTORY) {
            navController.navigate(Routes.INVENTORY) { launchSingleTop = true }
        }
    }

    CompositionLocalProvider(LocalAppMessages provides messages) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (isTab) {
                    when (currentRoute) {
                        Routes.DASHBOARD -> MyPenLargeTopBar(
                            title = stringResource(R.string.app_name),
                            subtitle = if (snapshot != null) {
                                DateUtils.displayDateLong(snapshot.today)
                            } else {
                                null
                            },
                            scrollBehavior = scrollBehavior
                        )
                        Routes.INVENTORY -> MyPenTopBar(
                            title = stringResource(R.string.nav_inventory),
                            subtitle = if (snapshot != null) {
                                stringResource(
                                    R.string.inventory_count_subtitle,
                                    PersianText.number(snapshot.activePens.size)
                                )
                            } else {
                                null
                            }
                        )
                        Routes.HISTORY -> MyPenTopBar(
                            title = stringResource(R.string.nav_history),
                            subtitle = if (snapshot != null) {
                                stringResource(
                                    R.string.history_count_subtitle,
                                    PersianText.number(snapshot.historyEvents.size)
                                )
                            } else {
                                null
                            }
                        )
                        else -> MyPenTopBar(title = stringResource(R.string.nav_settings))
                    }
                }
            },
            bottomBar = {
                if (isTab) {
                    MyPenBottomBar(
                        currentRoute = currentRoute,
                        expiringCount = snapshot?.metrics?.expiringSoonCount ?: 0,
                        onNavigate = { route ->
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (currentRoute == Routes.DASHBOARD || currentRoute == Routes.INVENTORY) {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate(Routes.ADD_PEN) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = {
                            Text(
                                text = stringResource(R.string.btn_add_pen),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = messages.hostState) { snackbarData ->
                    AppSnackbarHost(data = snackbarData)
                }
            }
        ) { innerPadding ->
            val uiData = data
            if (uiData == null) {
                LoadingSkeleton(modifier = Modifier.padding(innerPadding))
                return@Scaffold
            }

            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(Motion.MEDIUM)) +
                        slideInHorizontally(animationSpec = tween(Motion.MEDIUM)) { it / 14 }
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(Motion.SHORT))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(Motion.MEDIUM))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(Motion.SHORT)) +
                        slideOutHorizontally(animationSpec = tween(Motion.MEDIUM)) { it / 14 }
                }
            ) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        metrics = uiData.metrics,
                        activePens = uiData.activePens,
                        today = uiData.today,
                        onNavigateToInventory = { type ->
                            inventoryType = type
                            navController.navigate(Routes.INVENTORY) { launchSingleTop = true }
                        },
                        onNavigateToExpired = { navController.navigate(Routes.EXPIRED) },
                        onAddPen = { navController.navigate(Routes.ADD_PEN) },
                        onPenSelected = { penId -> navController.navigate(Routes.penDetails(penId)) }
                    )
                }

                composable(Routes.INVENTORY) {
                    InventoryScreen(
                        pens = uiData.activePens,
                        currentType = inventoryType,
                        today = uiData.today,
                        onTypeSelected = { inventoryType = it },
                        onPenSelected = { pen -> navController.navigate(Routes.penDetails(pen.id)) },
                        onMarkOpened = { pen ->
                            scope.launch(Dispatchers.IO) {
                                val result = runCatching {
                                    repository.markPenAsOpened(
                                        pen.id,
                                        DateUtils.todayDate(),
                                        DateUtils.todayDate()
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        messages.success(context.getString(R.string.msg_pen_opened))
                                        refreshAll()
                                    } else {
                                        messages.error(
                                            context.getString(R.string.msg_operation_failed) +
                                                " " + (result.exceptionOrNull()?.message ?: "")
                                        )
                                    }
                                }
                            }
                        },
                        onDeletePen = { pen ->
                            scope.launch(Dispatchers.IO) {
                                val result = runCatching { repository.deletePenPermanently(pen.id) }
                                withContext(Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        refreshAll()
                                        messages.success(
                                            context.getString(R.string.msg_pen_deleted)
                                        )
                                    } else {
                                        messages.error(
                                            context.getString(R.string.msg_operation_failed) +
                                                " " + (result.exceptionOrNull()?.message ?: "")
                                        )
                                    }
                                }
                            }
                        },
                        onAddPen = { navController.navigate(Routes.ADD_PEN) }
                    )
                }

                composable(Routes.HISTORY) {
                    HistoryScreen(
                        events = uiData.historyEvents,
                        today = uiData.today,
                        selectedFilter = historyFilter,
                        onFilterSelected = { historyFilter = it },
                        onEventClicked = { penId -> navController.navigate(Routes.penDetails(penId)) }
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        settings = uiData.settings,
                        onSettingsChanged = { newSettings ->
                            scope.launch(Dispatchers.IO) {
                                repository.saveNotificationSettings(newSettings)
                                refreshAll()
                            }
                        },
                        onExportBackup = { repository.exportBackupJson() },
                        onRestoreBackup = { json ->
                            // Intentionally synchronous: the settings screen shows
                            // failures through a snackbar and restore is rare.
                            repository.restoreFromJson(json)
                            refreshAll()
                        }
                    )
                }

                composable(Routes.ADD_PEN) {
                    AddPenScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSavePens = { name, type, expiry, count, onSuccess, onError ->
                            scope.launch(Dispatchers.IO) {
                                val result = runCatching {
                                    repository.addPens(name, type, expiry, count, DateUtils.todayDate())
                                }
                                withContext(Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        refreshAll()
                                        onSuccess()
                                    } else {
                                        onError(
                                            result.exceptionOrNull()?.message
                                                ?: context.getString(R.string.msg_operation_failed)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                composable(
                    route = Routes.PEN_DETAILS,
                    arguments = listOf(navArgument(Routes.ARG_PEN_ID) { type = NavType.StringType })
                ) { entry ->
                    val penId = entry.arguments?.getString(Routes.ARG_PEN_ID)
                    var penLoaded by remember(penId, dataVersion) { mutableStateOf(false) }
                    val pen by produceState<InsulinPen?>(initialValue = null, penId, dataVersion) {
                        value = withContext(Dispatchers.IO) { repository.getPenById(penId) }
                        penLoaded = true
                    }
                    val penHistory by produceState(
                        initialValue = emptyList<HistoryEvent>(),
                        penId,
                        dataVersion
                    ) {
                        value = withContext(Dispatchers.IO) { repository.getPenHistory(penId) }
                    }

                    PenDetailsScreen(
                        pen = pen,
                        historyEvents = penHistory,
                        today = uiData.today,
                        isLoading = !penLoaded,
                        onNavigateBack = { navController.popBackStack() },
                        onUpdatePen = { newName, newType, newExpiry ->
                            scope.launch(Dispatchers.IO) {
                                val result = runCatching {
                                    repository.updatePen(
                                        penId ?: return@launch,
                                        newName,
                                        newType,
                                        newExpiry,
                                        DateUtils.todayDate()
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        refreshAll()
                                        messages.success(context.getString(R.string.msg_pen_updated))
                                    } else {
                                        messages.error(
                                            context.getString(R.string.msg_operation_failed) +
                                                " " + (result.exceptionOrNull()?.message ?: "")
                                        )
                                    }
                                }
                            }
                        },
                        onMarkOpened = { openedDate ->
                            scope.launch(Dispatchers.IO) {
                                val result = runCatching {
                                    repository.markPenAsOpened(
                                        penId ?: return@launch,
                                        openedDate,
                                        DateUtils.todayDate()
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        refreshAll()
                                        messages.success(context.getString(R.string.msg_pen_opened))
                                    } else {
                                        messages.error(
                                            context.getString(R.string.msg_operation_failed) +
                                                " " + (result.exceptionOrNull()?.message ?: "")
                                        )
                                    }
                                }
                            }
                        },
                        onMarkDefective = { reason ->
                            scope.launch(Dispatchers.IO) {
                                val result = runCatching {
                                    repository.markPenAsDefective(
                                        penId ?: return@launch,
                                        reason,
                                        DateUtils.todayDate()
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        refreshAll()
                                        messages.success(context.getString(R.string.msg_pen_defective))
                                    } else {
                                        messages.error(
                                            context.getString(R.string.msg_operation_failed) +
                                                " " + (result.exceptionOrNull()?.message ?: "")
                                        )
                                    }
                                }
                            }
                        },
                        onDeletePen = {
                            scope.launch(Dispatchers.IO) {
                                val result = runCatching {
                                    repository.deletePenPermanently(penId ?: return@launch)
                                }
                                withContext(Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        refreshAll()
                                        messages.success(context.getString(R.string.msg_pen_deleted))
                                        navController.popBackStack()
                                    } else {
                                        messages.error(
                                            context.getString(R.string.msg_operation_failed) +
                                                " " + (result.exceptionOrNull()?.message ?: "")
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                composable(Routes.EXPIRED) {
                    val expiredPens by produceState(
                        initialValue = emptyList<InsulinPen>(),
                        dataVersion
                    ) {
                        value = withContext(Dispatchers.IO) { repository.getExpiredPens() }
                    }
                    ExpiredPensScreen(
                        pens = expiredPens,
                        today = uiData.today,
                        onNavigateBack = { navController.popBackStack() },
                        onPenSelected = { pen -> navController.navigate(Routes.penDetails(pen.id)) }
                    )
                }
            }
        }
    }
}
