package com.helper.beamnetworks

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class BeamNetworksApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Caller ID"
            val descriptionText = "Displays caller information for incoming calls."
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("caller_id_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}