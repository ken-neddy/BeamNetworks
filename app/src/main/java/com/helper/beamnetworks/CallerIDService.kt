package com.helper.beamnetworks

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.location.Geocoder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale
import kotlin.math.abs

class CallerIDService : Service() {

    private lateinit var windowManager: WindowManager
    private var popupView: View? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phoneNumber")
        if (phoneNumber != null) {
            Toast.makeText(this, "CallerIDService looking for: $phoneNumber", Toast.LENGTH_LONG).show()
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
                    val message = if (snapshot.exists()) "Match found in database for $phoneNumber" else "No match found in database for $phoneNumber"
                    Toast.makeText(this@CallerIDService, message, Toast.LENGTH_LONG).show()

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
                    Toast.makeText(this@CallerIDService, "Database query cancelled: ${error.message}", Toast.LENGTH_LONG).show()
                    stopSelf()
                }
            })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopup(installation: InstallationData) {
        if (popupView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.caller_id_popup, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 400 // Initial position
        }

        popupView?.findViewById<TextView>(R.id.caller_name)?.text = installation.clientName
        popupView?.findViewById<TextView>(R.id.caller_number)?.text = installation.clientPhone
        popupView?.findViewById<TextView>(R.id.caller_status)?.text = installation.status + " installation"

        val locationTextView = popupView?.findViewById<TextView>(R.id.caller_location)
        val locationString = installation.clientLocation

        if (locationString != null && locationString.contains(",")) {
            try {
                val parts = locationString.split(",")
                val latitude = parts[0].trim().toDouble()
                val longitude = parts[1].trim().toDouble()

                // Geocoding must happen in a background thread to avoid blocking the UI
                Thread {
                    var readableAddress = locationString // Default to original string
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                        if (addresses != null && addresses.isNotEmpty()) {
                            val address = addresses[0]
                            // Use feature name (like a park or building), or thoroughfare (street), or the full first address line
                            readableAddress = address.featureName ?: address.thoroughfare ?: address.getAddressLine(0)
                        }
                    } catch (e: IOException) {
                        // Geocoder can fail, so we just fall back to the original string
                    }

                    // Update UI on the main thread
                    Handler(Looper.getMainLooper()).post {
                        locationTextView?.text = readableAddress
                    }
                }.start()
            } catch (e: NumberFormatException) {
                // If parsing lat/long fails, just show the original string
                locationTextView?.text = locationString
            }
        } else {
            // If it's not in a coordinate format, show it as is
            locationTextView?.text = locationString
        }

        val closeButton = popupView?.findViewById<ImageButton>(R.id.close_button)
        closeButton?.setOnClickListener { stopSelf() }

        var initialX = 0f
        var initialY = 0f
        var initialYPos = 0

        popupView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    initialY = event.rawY
                    initialYPos = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    params.y = initialYPos + deltaY.toInt()
                    windowManager.updateViewLayout(popupView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialX
                    if (abs(deltaX) > 100) { // Swipe threshold to close
                        stopSelf()
                    }
                    view.performClick() // For accessibility
                    true
                }
                else -> false
            }
        }

        windowManager.addView(popupView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (popupView != null && popupView?.windowToken != null) {
            windowManager.removeView(popupView)
            popupView = null
        }
    }
}
