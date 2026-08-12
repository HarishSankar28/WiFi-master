package com.wifisense.motiontracker.data.processing

import com.wifisense.motiontracker.domain.model.MotionEvent
import com.wifisense.motiontracker.domain.model.MotionState
import com.wifisense.motiontracker.domain.model.SignalSample
import com.wifisense.motiontracker.util.Constants
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Core signal processing engine for WiFi-based motion detection.
 *
 * Pipeline:
 *  Raw RSSI → Moving Average Filter → Rolling Variance → Motion Classification
 *
 * Variance thresholds (configurable via sensitivity):
 *  < 2   dBm² → STATIONARY
 *  2–8   dBm² → MINOR_MOTION
 *  8–20  dBm² → MODERATE_MOTION
 *  > 20  dBm² → HEAVY_MOTION
 */
@Singleton
class SignalProcessor @Inject constructor() {

    // Rolling window of raw RSSI values
    private val rawWindow = LinkedList<Int>()

    // Moving-average smoothed values for variance calculation
    private val smoothedWindow = LinkedList<Float>()

    // Baseline variance from calibration (subtracted to remove env noise)
    private var baselineVariance: Double = 0.0

    // Sensitivity multiplier (1=low … 5=high sensitivity)
    private var sensitivityLevel: Int = 3

    // Current state (for stability / debouncing)
    private var lastState: MotionState = MotionState.CALIBRATING
    private var stateCount: Int = 0
    private val STATE_DEBOUNCE = 3  // require N consecutive same-state to switch

    /**
     * Feed a new signal sample into the processor.
     * Returns a [MotionEvent] once enough data is collected, or null during warm-up.
     */
    fun process(sample: SignalSample): MotionEvent? {
        // 1. Update raw window
        rawWindow.add(sample.rssi)
        if (rawWindow.size > Constants.ROLLING_WINDOW_SIZE) {
            rawWindow.removeFirst()
        }

        // Need minimum samples before processing
        if (rawWindow.size < Constants.MOVING_AVG_WINDOW) return null

        // 2. Apply moving average filter
        val smoothed = rawWindow.takeLast(Constants.MOVING_AVG_WINDOW)
            .average()
            .toFloat()
        smoothedWindow.add(smoothed)
        if (smoothedWindow.size > Constants.ROLLING_WINDOW_SIZE) {
            smoothedWindow.removeFirst()
        }

        if (smoothedWindow.size < 5) return null

        // 3. Compute rolling variance of smoothed values
        val mean = smoothedWindow.average()
        val variance = smoothedWindow.sumOf { (it - mean).pow(2) } / smoothedWindow.size

        // 4. Subtract baseline noise floor (from calibration)
        val adjustedVariance = maxOf(0.0, variance - baselineVariance)

        // 5. Apply sensitivity scaling
        val scaledVariance = adjustedVariance * sensitivityMultiplier()

        // 6. Classify motion state
        val candidateState = classifyVariance(scaledVariance)

        // 7. Debounce state transitions
        val finalState = debounce(candidateState)

        return MotionEvent(
            timestamp = sample.timestamp,
            motionState = finalState,
            rssiVariance = scaledVariance,
            smoothedRssi = smoothed,
            rawRssi = sample.rssi,
            routerSsid = sample.ssid,
            routerBssid = sample.bssid
        )
    }

    /**
     * Set the calibration baseline variance.
     * Called after the 30-second empty-room calibration phase.
     */
    fun setBaselineVariance(baseline: Double) {
        this.baselineVariance = baseline
    }

    /**
     * Update sensitivity (1=low, 5=high).
     * Higher sensitivity → detect even subtle motion.
     */
    fun setSensitivity(level: Int) {
        sensitivityLevel = level.coerceIn(1, 5)
    }

    /** Reset the processor state (e.g., when starting a new session) */
    fun reset() {
        rawWindow.clear()
        smoothedWindow.clear()
        lastState = MotionState.CALIBRATING
        stateCount = 0
    }

    /** Compute variance of a list of RSSI values (used for calibration) */
    fun computeVarianceOf(samples: List<Int>): Double {
        if (samples.isEmpty()) return 0.0
        val mean = samples.average()
        return samples.sumOf { (it - mean).pow(2) } / samples.size
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private fun classifyVariance(variance: Double): MotionState = when {
        variance < Constants.THRESHOLD_MINOR_MOTION    -> MotionState.STATIONARY
        variance < Constants.THRESHOLD_MODERATE_MOTION -> MotionState.MINOR_MOTION
        variance < Constants.THRESHOLD_HEAVY_MOTION    -> MotionState.MODERATE_MOTION
        else                                            -> MotionState.HEAVY_MOTION
    }

    private fun debounce(candidate: MotionState): MotionState {
        return if (candidate == lastState) {
            stateCount++
            lastState
        } else {
            stateCount++
            if (stateCount >= STATE_DEBOUNCE) {
                stateCount = 0
                lastState = candidate
            }
            lastState
        }
    }

    private fun sensitivityMultiplier(): Double = when (sensitivityLevel) {
        1 -> 0.5
        2 -> 0.75
        3 -> 1.0
        4 -> 1.5
        5 -> 2.0
        else -> 1.0
    }

    private fun Double.pow(n: Int): Double = Math.pow(this, n.toDouble())
    private fun Float.pow(n: Int): Double = Math.pow(this.toDouble(), n.toDouble())
}
