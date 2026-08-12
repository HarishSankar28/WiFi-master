package com.wifisense.motiontracker.domain.model

/**
 * Represents a single WiFi signal sample captured from the router.
 *
 * @param timestamp Unix epoch milliseconds when the sample was captured
 * @param ssid      Network name (SSID) of the access point
 * @param bssid     Hardware address (BSSID/MAC) of the access point
 * @param rssi      Received Signal Strength Indicator in dBm (e.g., -60 dBm)
 * @param frequency Channel frequency in MHz (2412 = 2.4GHz ch1, 5180 = 5GHz ch36)
 * @param linkSpeed Current link speed in Mbps (connected AP only)
 */
data class SignalSample(
    val timestamp: Long,
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val linkSpeed: Int = -1
) {
    /** Returns frequency band as a human-readable string */
    val band: String get() = if (frequency > 4000) "5 GHz" else "2.4 GHz"

    /** Returns quality as 0-100% based on RSSI */
    val qualityPercent: Int get() {
        return when {
            rssi >= -50 -> 100
            rssi >= -60 -> 80
            rssi >= -70 -> 60
            rssi >= -80 -> 40
            rssi >= -90 -> 20
            else -> 0
        }
    }
}
