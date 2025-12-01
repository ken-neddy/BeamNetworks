package com.helper.beamnetworks

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.CountDownLatch

class InstallationReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val latch = CountDownLatch(1)
        val database = FirebaseDatabase.getInstance()
        val installationsRef = database.getReference("installations")

        val threeDaysFromNow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 3)
        }.time

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        installationsRef.orderByChild("status").equalTo("Upcoming")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val installation = child.getValue(InstallationData::class.java)
                        installation?.let {
                            try {
                                val installationDate = dateFormat.parse(it.installationDate)
                                if (installationDate != null && isSameDay(installationDate, threeDaysFromNow)) {
                                    sendNotification(it)
                                }
                            } catch (e: Exception) {
                                // Ignore malformed dates
                            }
                        }
                    }
                    latch.countDown()
                }

                override fun onCancelled(error: DatabaseError) {
                    latch.countDown()
                }
            })

        latch.await()
        return Result.success()
    }

    private fun isSameDay(date1: java.util.Date, date2: java.util.Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun sendNotification(installation: InstallationData) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("beamnetworks://app/installation_details/${installation.id}")
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val soundUri = Uri.parse("android.resource://${applicationContext.packageName}/" + R.raw.beam_networks_notification)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, "installation_reminder_channel")
            .setContentTitle("Upcoming Installation Reminder")
            .setContentText("Installation for ${installation.clientName} is scheduled for ${installation.installationDate}.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .build()
        notificationManager.notify(installation.id.hashCode(), notification)
    }
}