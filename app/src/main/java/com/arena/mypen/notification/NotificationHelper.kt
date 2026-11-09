package com.arena.mypen.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.arena.mypen.MainActivity
import com.arena.mypen.R
import com.arena.mypen.data.PenRepository
import com.arena.mypen.model.InsulinPen
import com.arena.mypen.util.DateUtils
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_ID = "mypen_expiry_channel"
    const val CHANNEL_NAME = "اعلان‌های انقضای انسولین"
    private const val WORK_NAME = "mypen_expiry_periodic_work"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان‌های یادآوری تاریخ انقضای قلم‌های انسولین"
                enableVibration(true)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun schedulePeriodicCheck(context: Context) {
        val workRequest = PeriodicWorkRequest.Builder(
            ExpiryCheckWorker::class.java,
            24, TimeUnit.HOURS
        ).setConstraints(Constraints.Builder().build()).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun checkAndSendNotifications(context: Context): Int {
        createNotificationChannel(context)

        val repo = PenRepository.getInstance(context)
        val today = DateUtils.todayDate()
        val settings = repo.getNotificationSettings()

        val activePens = repo.getAllActivePens(today)
        var sentCount = 0

        for (pen in activePens) {
            val days = DateUtils.calculateDaysRemaining(pen.expiryDate, today)

            // Fire an enabled, not-yet-sent threshold as soon as the pen is
            // within its window (daysRemaining <= threshold), instead of only
            // on the exact day. The notification_logs table prevents repeats.
            val thresholds = listOf(
                30 to settings.notify30Days,
                14 to settings.notify14Days,
                7 to settings.notify7Days
            )
            for ((thresholdDays, enabled) in thresholds) {
                if (!enabled) continue
                if (days !in 0..thresholdDays.toLong()) continue
                if (repo.hasNotified(pen.id, thresholdDays)) continue
                if (sendNotification(context, pen, thresholdDays)) {
                    repo.recordNotification(pen.id, thresholdDays)
                    sentCount++
                }
            }
        }

        return sentCount
    }

    private fun sendNotification(context: Context, pen: InsulinPen, thresholdDays: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("OPEN_TAB", "INVENTORY")
            putExtra("PEN_TYPE", pen.type.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Deterministic, always non-negative, and bounded to Int range.
        val requestCode = (pen.id.hashCode() and 0x7fffffff) % 1_000_000_000 + thresholdDays
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "یادآوری انقضای انسولین (${DateUtils.formatEnglish(thresholdDays)} روز باقی‌مانده)"
        val message = "قلم ${pen.name} (${pen.type.persianLabel}) در تاریخ ${pen.expiryDate} منقضی می‌شود. لطفاً اولویت مصرف را رعایت فرمایید."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            NotificationManagerCompat.from(context).notify(requestCode, builder.build())
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
