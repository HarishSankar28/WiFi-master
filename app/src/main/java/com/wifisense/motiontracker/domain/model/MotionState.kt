package com.wifisense.motiontracker.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Represents the detected motion state of the monitored space.
 * Classification is based on rolling RSSI variance analysis.
 */
enum class MotionState(
    val label: String,
    val description: String,
    val colorHex: Long,
    val intensityLevel: Int   // 0–4 used for UI animations
) {
    CALIBRATING(
        label = "Calibrating",
        description = "Establishing baseline signal fingerprint…",
        colorHex = 0xFF9E9E9E,
        intensityLevel = 0
    ),
    STATIONARY(
        label = "Stationary",
        description = "No motion detected. Room appears empty.",
        colorHex = 0xFF00E5FF,
        intensityLevel = 1
    ),
    MINOR_MOTION(
        label = "Minor Motion",
        description = "Subtle movement detected — breathing, shifting.",
        colorHex = 0xFF69F0AE,
        intensityLevel = 2
    ),
    MODERATE_MOTION(
        label = "Active Motion",
        description = "Person walking or moving around the room.",
        colorHex = 0xFFFFB300,
        intensityLevel = 3
    ),
    HEAVY_MOTION(
        label = "Vigorous Motion",
        description = "High activity — running or rapid movement.",
        colorHex = 0xFFFF5252,
        intensityLevel = 4
    );

    fun toColor(): Color = Color(colorHex)
}
