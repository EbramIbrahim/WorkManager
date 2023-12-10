package com.example.workmanager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class DownloadApplication: Application() {


    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            "download_channel",
            "image_notification",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}








