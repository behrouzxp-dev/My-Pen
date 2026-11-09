package com.mypen.app

import android.app.Application
import com.mypen.app.notification.NotificationHelper

class MyPenApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.schedulePeriodicCheck(this)
    }
}
