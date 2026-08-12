package com.wifisense.motiontracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * BroadcastReceiver that auto-starts the motion sensing service after device reboot
 * if the user had background monitoring enabled.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Only auto-start if background monitoring was enabled
            val prefs = context.getSharedPreferences("motion_tracker_prefs", Context.MODE_PRIVATE)
            val bgEnabled = prefs.getBoolean("background_service_enabled", false)
            if (bgEnabled) {
                val bssid = prefs.getString("pinned_bssid", null)
                val baseline = prefs.getFloat("calibration_baseline_variance", 0f).toDouble()
                val sensitivity = prefs.getInt("sensitivity_level", 3)
                ContextCompat.startForegroundService(
                    context,
                    MotionSensingService.startIntent(context, bssid, baseline, sensitivity)
                )
            }
        }
    }
}
