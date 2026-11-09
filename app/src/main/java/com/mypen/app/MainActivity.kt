package com.mypen.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mypen.app.data.PenRepository
import com.mypen.app.model.InsulinType
import com.mypen.app.notification.NotificationHelper
import com.mypen.app.ui.components.Tabs
import com.mypen.app.ui.nav.MyPenApp
import com.mypen.app.ui.theme.MyPenTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: PenRepository

    /**
     * Deep-link target. It is a Compose state so that a notification tapped while
     * the activity is already alive still opens the requested pen.
     */
    private val pendingPenId = androidx.compose.runtime.mutableStateOf<String?>(null)
    private val pendingTab = androidx.compose.runtime.mutableStateOf(Tabs.DASHBOARD)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        repository = PenRepository.getInstance(this)
        requestNotificationPermissionIfNeeded()
        consumeIntent(intent)

        setContent {
            MyPenTheme {
                MyPenApp(
                    repository = repository,
                    initialTab = pendingTab.value,
                    initialType = parseInitialType(intent),
                    initialPenId = pendingPenId.value
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Never touch the database or post notifications on the UI thread.
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { NotificationHelper.checkAndSendNotifications(this@MainActivity) }
        }
    }

    private fun consumeIntent(intent: Intent?) {
        if (intent == null) return
        pendingPenId.value = intent.getStringExtra(EXTRA_PEN_ID)
        pendingTab.value = if (intent.getStringExtra(EXTRA_OPEN_TAB) == OPEN_TAB_INVENTORY) {
            Tabs.INVENTORY
        } else {
            Tabs.DASHBOARD
        }
    }

    private fun parseInitialType(intent: Intent?): InsulinType {
        return InsulinType.fromString(intent?.getStringExtra(EXTRA_PEN_TYPE))
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_TAB = "OPEN_TAB"
        const val EXTRA_PEN_TYPE = "PEN_TYPE"
        const val EXTRA_PEN_ID = "PEN_ID"
        const val OPEN_TAB_INVENTORY = "INVENTORY"
    }
}
