package com.helper.beamnetworks

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CallerIDService : Service() {

    private lateinit var windowManager: WindowManager
    private var popupView: android.view.View? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phoneNumber")
        if (phoneNumber != null) {
            val notification = createNotification()
            startForeground(1, notification)
            fetchCallerInfo(phoneNumber)
        }
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "caller_id_channel")
            .setContentTitle("Beam Networks Caller ID")
            .setContentText("Checking for caller information...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun fetchCallerInfo(phoneNumber: String) {
        val database = FirebaseDatabase.getInstance()
        val installationsRef = database.getReference("installations")

        installationsRef.orderByChild("clientPhone").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val installation = child.getValue(InstallationData::class.java)
                            if (installation != null) {
                                Handler(Looper.getMainLooper()).post {
                                    showPopup(installation)
                                }
                                break
                            }
                        }
                    } else {
                        stopSelf()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    stopSelf()
                }
            })
    }

    private fun showPopup(installation: InstallationData) {
        if (popupView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.caller_id_popup, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        popupView?.findViewById<TextView>(R.id.caller_name)?.text = installation.clientName
        popupView?.findViewById<TextView>(R.id.caller_number)?.text = installation.clientPhone
        popupView?.findViewById<TextView>(R.id.caller_status)?.text = installation.status
        popupView?.findViewById<TextView>(R.id.caller_location)?.text = installation.clientLocation

        windowManager.addView(popupView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (popupView != null) {
             Handler(Looper.getMainLooper()).post {
                if (popupView?.windowToken != null) {
                    windowManager.removeView(popupView)
                    popupView = null
                }
            }
        }
    }
}