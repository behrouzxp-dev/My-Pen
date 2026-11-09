package com.mypen.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.mypen.app.data.PenRepository
import com.mypen.app.util.DateUtils
import com.mypen.app.widget.MyPenWidgetProvider

/**
 * Handles the inline actions attached to expiry notifications, so a pen can be
 * marked as opened without opening the application.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val penId = intent.getStringExtra(EXTRA_PEN_ID) ?: return
        if (intent.action != ACTION_MARK_OPENED) return

        runCatching {
            val repository = PenRepository.getInstance(context)
            val today = DateUtils.todayDate()
            repository.markPenAsOpened(penId, today, today)
        }

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId >= 0) {
            runCatching {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
        }

        runCatching { MyPenWidgetProvider.updateAll(context) }
    }

    companion object {
        const val ACTION_MARK_OPENED = "com.mypen.app.action.MARK_PEN_OPENED"
        const val EXTRA_PEN_ID = "penId"
        const val EXTRA_NOTIFICATION_ID = "notificationId"
    }
}
