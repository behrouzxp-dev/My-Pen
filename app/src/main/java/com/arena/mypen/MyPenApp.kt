package com.arena.mypen

import android.app.Application
import com.arena.mypen.notification.NotificationHelper

class MyPenApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.schedulePeriodicCheck(this)
    }
}
