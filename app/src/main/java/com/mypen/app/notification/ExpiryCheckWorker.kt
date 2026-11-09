package com.mypen.app.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ExpiryCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            NotificationHelper.checkAndSendNotifications(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
