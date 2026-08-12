package com.wifisense.motiontracker.data.wifi

import com.wifisense.motiontracker.domain.model.SignalSample
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that exposes WiFi signal samples to the domain layer.
 * Acts as the single source of truth for raw signal data.
 */
@Singleton
class WifiScanRepository @Inject constructor(
    private val wifiScanner: WifiScanner
) {
    /**
     * Returns a [Flow] of [SignalSample] objects.
     * Optionally pin to a specific router BSSID for focused tracking.
     */
    fun signalFlow(targetBssid: String? = null): Flow<SignalSample> =
        wifiScanner.scanFlow(targetBssid)

    /** Whether WiFi hardware is enabled on the device */
    fun isWifiEnabled(): Boolean = wifiScanner.isWifiEnabled()

    /** BSSID of the currently connected AP */
    fun getConnectedBssid(): String? = wifiScanner.getConnectedBssid()

    /** SSID of the currently connected AP */
    fun getConnectedSsid(): String? = wifiScanner.getConnectedSsid()
}
