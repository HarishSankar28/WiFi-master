package com.wifisense.motiontracker.domain.model

/**
 * Represents a completed monitoring session stored in the local database.
 *
 * @param id                Unique session identifier
 * @param startTime         Session start timestamp (epoch ms)
 * @param endTime           Session end timestamp (epoch ms)
 * @param routerSsid        SSID of the monitored router
 * @param routerBssid       BSSID of the monitored router
 * @param dominantState     The most frequent motion state during the session
 * @param averageVariance   Mean RSSI variance across the session
 * @param peakVariance      Maximum observed RSSI variance
 * @param stationaryMinutes Minutes of stationary state
 * @param activeMinutes     Minutes of motion (all levels combined)
 * @param eventCount        Total motion events recorded
 */
data class ActivitySession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val routerSsid: String,
    val routerBssid: String,
    val dominantState: MotionState,
    val averageVariance: Double,
    val peakVariance: Double,
    val stationaryMinutes: Int,
    val activeMinutes: Int,
    val eventCount: Int
) {
    /** Duration of the session in milliseconds */
    val durationMs: Long get() = endTime - startTime

    /** Duration as human-readable string */
    val durationFormatted: String get() {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    /** Activity ratio as 0–100% */
    val activityPercent: Int get() {
        val total = stationaryMinutes + activeMinutes
        return if (total == 0) 0 else (activeMinutes * 100 / total)
    }
}
