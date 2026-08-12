package com.wifisense.motiontracker.domain.usecase

import com.wifisense.motiontracker.data.processing.SignalProcessor
import com.wifisense.motiontracker.data.wifi.WifiScanRepository
import com.wifisense.motiontracker.domain.model.MotionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

/**
 * Use case that starts the WiFi-based motion monitoring pipeline.
 *
 * Flow: WifiScanner → SignalProcessor → MotionEvent stream
 *
 * @param targetBssid Optional BSSID to pin monitoring to a specific AP.
 *                    If null, uses the currently connected AP.
 * @param baselineVariance Calibration baseline from stored prefs.
 * @param sensitivity Sensitivity level 1–5.
 */
class StartMonitoringUseCase @Inject constructor(
    private val wifiScanRepository: WifiScanRepository,
    private val signalProcessor: SignalProcessor
) {
    operator fun invoke(
        targetBssid: String? = null,
        baselineVariance: Double = 0.0,
        sensitivity: Int = 3
    ): Flow<MotionEvent> {
        signalProcessor.reset()
        signalProcessor.setBaselineVariance(baselineVariance)
        signalProcessor.setSensitivity(sensitivity)

        return wifiScanRepository
            .signalFlow(targetBssid)
            .mapNotNull { sample -> signalProcessor.process(sample) }
    }
}
