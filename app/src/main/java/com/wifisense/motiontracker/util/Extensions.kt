package com.wifisense.motiontracker.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── RSSI / Signal ────────────────────────────────────────────────────────────

/** Convert RSSI dBm value to 0–100% signal quality */
fun Int.rssiToQualityPercent(): Int = when {
    this >= -50 -> 100
    this >= -60 -> 80
    this >= -70 -> 60
    this >= -80 -> 40
    this >= -90 -> 20
    else -> 0
}

/** Convert RSSI to signal quality label */
fun Int.rssiToQualityLabel(): String = when {
    this >= -50 -> "Excellent"
    this >= -60 -> "Good"
    this >= -70 -> "Fair"
    this >= -80 -> "Weak"
    else -> "Very Weak"
}

/** Format RSSI as display string */
fun Int.formatRssi(): String = "${this} dBm"

// ─── Time / Duration ──────────────────────────────────────────────────────────

/** Format epoch ms as readable date/time */
fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

/** Format epoch ms as just the date */
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

/** Format epoch ms as just the time */
fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

/** Format duration in milliseconds as human-readable string */
fun Long.toFormattedDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0   -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else        -> "${seconds}s"
    }
}

/** Format seconds as mm:ss */
fun Long.toMinuteSeconds(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// ─── Variance ─────────────────────────────────────────────────────────────────

/** Format variance as 2-decimal string */
fun Double.formatVariance(): String = "%.2f dBm²".format(this)

/** Normalize variance to 0–1 for UI progress indicators */
fun Double.normalizeVariance(maxVariance: Double = 30.0): Float =
    (this / maxVariance).coerceIn(0.0, 1.0).toFloat()
