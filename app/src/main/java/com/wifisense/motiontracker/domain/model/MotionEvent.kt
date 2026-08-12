package com.wifisense.motiontracker.domain.model

/**
 * A processed motion event emitted by the signal processing pipeline.
 *
 * @param timestamp     When this event was computed
 * @param motionState   Classified motion level
 * @param rssiVariance  Rolling variance of RSSI used to determine the state
 * @param smoothedRssi  Filtered/smoothed RSSI value in dBm
 * @param rawRssi       Unfiltered raw RSSI reading
 * @param routerSsid    SSID of the monitored router
 * @param routerBssid   BSSID of the monitored router
 */
data class MotionEvent(
    val timestamp: Long,
    val motionState: MotionState,
    val rssiVariance: Double,
    val smoothedRssi: Float,
    val rawRssi: Int,
    val routerSsid: String,
    val routerBssid: String
)
