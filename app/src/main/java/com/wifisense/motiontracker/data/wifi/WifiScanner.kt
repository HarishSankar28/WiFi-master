package com.wifisense.motiontracker.data.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import com.wifisense.motiontracker.domain.model.SignalSample
import com.wifisense.motiontracker.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages WiFi signal scanning using two complementary strategies:
 *
 * 1. **Full Scan** (via WifiManager.startScan): captures ALL visible APs every 30s.
 *    Limited by Android throttling to 4 scans / 2 minutes.
 *
 * 2. **RSSI Poll** (via WifiManager.calculateSignalLevel): reads the currently
 *    connected AP's RSSI every 2 seconds. No throttle limit. Provides high-
 *    frequency signal data for accurate motion detection.
 *
 * Both flows are merged into a single Flow<SignalSample> stream.
 */
@Singleton
class WifiScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val handler = Handler(Looper.getMainLooper())

    /** Emits signal samples from both full scans and rapid RSSI polling */
    fun scanFlow(targetBssid: String? = null): Flow<SignalSample> =
        merge(fullScanFlow(targetBssid), rssiPollFlow())

    // ─── Strategy 1: Full WiFi Scan ──────────────────────────────────────────

    /**
     * Triggers periodic full WiFi scans and emits [SignalSample] for each result.
     * Focuses on the target BSSID when provided, or auto-detects UBIQCOM router.
     */
    private fun fullScanFlow(targetBssid: String? = null): Flow<SignalSample> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return

                @Suppress("DEPRECATION")
                val results = wifiManager.scanResults ?: return

                val now = System.currentTimeMillis()
                results.forEach { result ->
                    // Filter: only emit if matches target or contains UBIQCOM keyword
                    val matchesBssid = targetBssid == null || result.BSSID == targetBssid
                    val isUbiqcom = result.SSID.contains(Constants.ROUTER_SSID_KEYWORD, ignoreCase = true)
                    if (matchesBssid || isUbiqcom || targetBssid == null) {
                        launch {
                            trySend(
                                SignalSample(
                                    timestamp = now,
                                    ssid = result.SSID ?: "",
                                    bssid = result.BSSID ?: "",
                                    rssi = result.level,
                                    frequency = result.frequency
                                )
                            )
                        }
                    }
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )

        // Schedule periodic scans
        val scanRunnable = object : Runnable {
            override fun run() {
                @Suppress("DEPRECATION")
                wifiManager.startScan()
                handler.postDelayed(this, Constants.SCAN_INTERVAL_MS)
            }
        }
        handler.post(scanRunnable)

        awaitClose {
            handler.removeCallbacks(scanRunnable)
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        }
    }

    // ─── Strategy 2: High-frequency RSSI Poll (Connected AP) ─────────────────

    /**
     * Polls RSSI of the currently connected AP every 2 seconds.
     * No Android throttling applies. Provides smooth, continuous signal data.
     */
    private fun rssiPollFlow(): Flow<SignalSample> = callbackFlow {
        val pollRunnable = object : Runnable {
            override fun run() {
                val info = wifiManager.connectionInfo
                if (info != null && info.bssid != null) {
                    @Suppress("DEPRECATION")
                    val rssi = info.rssi
                    trySend(
                        SignalSample(
                            timestamp = System.currentTimeMillis(),
                            ssid = info.ssid?.removeSurrounding("\"") ?: "",
                            bssid = info.bssid ?: "",
                            rssi = rssi,
                            frequency = info.frequency,
                            linkSpeed = info.linkSpeed
                        )
                    )
                }
                handler.postDelayed(this, Constants.RSSI_POLL_INTERVAL_MS)
            }
        }
        handler.post(pollRunnable)

        awaitClose {
            handler.removeCallbacks(pollRunnable)
        }
    }

    /** Returns the BSSID of the currently connected AP, or null */
    fun getConnectedBssid(): String? {
        @Suppress("DEPRECATION")
        return wifiManager.connectionInfo?.bssid
    }

    /** Returns the SSID of the currently connected AP, or null */
    fun getConnectedSsid(): String? {
        @Suppress("DEPRECATION")
        return wifiManager.connectionInfo?.ssid?.removeSurrounding("\"")
    }

    /** Whether WiFi is currently enabled */
    fun isWifiEnabled(): Boolean = wifiManager.isWifiEnabled
}
