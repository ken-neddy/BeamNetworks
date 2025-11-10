package com.helper.beamnetworks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.widget.Toast

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            return
        }

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            // --- DIAGNOSTIC TOAST ---
            val diagnosticMessage = "CallReceiver: Ringing. Number available: ${phoneNumber != null}"
            Toast.makeText(context, diagnosticMessage, Toast.LENGTH_LONG).show()

            if (phoneNumber != null) {
                val serviceIntent = Intent(context, CallerIDService::class.java)
                serviceIntent.putExtra("phoneNumber", phoneNumber)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        } else if (state == TelephonyManager.EXTRA_STATE_IDLE || state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            val serviceIntent = Intent(context, CallerIDService::class.java)
            context.stopService(serviceIntent)
        }
    }
}