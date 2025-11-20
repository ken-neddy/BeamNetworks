package com.helper.beamnetworks

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.Uri
import android.os.Build
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.helper.beamnetworks.R
import java.util.concurrent.TimeUnit

class BeamNetworksApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleInstallationReminder()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse("android.resource://$packageName/" + R.raw.beam_networks_notification)

            // Channel for Caller ID (remains low importance and silent)
            val callerIdChannel = NotificationChannel(
                "caller_id_channel",
                "Caller ID",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays caller information for incoming calls."
            }

            // V4 channel for Installation Reminders with custom sound
            val reminderChannel = NotificationChannel(
                "installation_reminder_channel_v4", // New ID
                "Installation Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming installations."
                setSound(soundUri, null)
            }

            // V4 channel for New Installation confirmations with custom sound
            val newInstallationChannel = NotificationChannel(
                "new_installation_channel_v4", // New ID
                "New Installations",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for newly logged installations."
                setSound(soundUri, null)
            }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(callerIdChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(newInstallationChannel)
        }
    }

    private fun scheduleInstallationReminder() {
        val reminderWorkRequest = PeriodicWorkRequestBuilder<InstallationReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueue(reminderWorkRequest)
    }
}