package com.wifisense.motiontracker.ui.screen.dashboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.wifisense.motiontracker.domain.model.MotionState
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated radar/pulse visual that responds to the current [MotionState].
 * Shows expanding concentric rings and a pulsing core dot.
 */
@Composable
fun MotionRadar(
    motionState: MotionState,
    modifier: Modifier = Modifier
) {
    val motionColor = motionState.toColor()
    val intensity = motionState.intensityLevel

    // Pulse animation — speed proportional to motion intensity
    val pulseDurationMs = when (motionState) {
        MotionState.CALIBRATING    -> 2000
        MotionState.STATIONARY     -> 3000
        MotionState.MINOR_MOTION   -> 1500
        MotionState.MODERATE_MOTION -> 900
        MotionState.HEAVY_MOTION   -> 500
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")

    val ring1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "ring1"
    )
    val ring2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMs, easing = LinearEasing, delayMillis = pulseDurationMs / 3),
            repeatMode = RepeatMode.Restart
        ), label = "ring2"
    )
    val ring3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMs, easing = LinearEasing, delayMillis = (pulseDurationMs * 2) / 3),
            repeatMode = RepeatMode.Restart
        ), label = "ring3"
    )

    // Core breathing animation
    val coreScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "core_scale"
    )

    // Rotation for HEAVY_MOTION sweep effect
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "sweep"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) / 2f

            // Static background rings (grid)
            drawStaticRings(center, maxRadius, motionColor)

            // Animated expanding pulse rings
            drawPulseRing(center, maxRadius, ring1, motionColor)
            if (intensity >= 2) drawPulseRing(center, maxRadius, ring2, motionColor)
            if (intensity >= 3) drawPulseRing(center, maxRadius, ring3, motionColor)

            // Radar sweep for heavy motion
            if (motionState == MotionState.HEAVY_MOTION || motionState == MotionState.MODERATE_MOTION) {
                drawRadarSweep(center, maxRadius, sweepAngle, motionColor)
            }

            // Core pulsing dot
            val coreRadius = (maxRadius * 0.18f) * coreScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(motionColor, motionColor.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = coreRadius * 1.5f
                ),
                radius = coreRadius * 1.5f,
                center = center
            )
            drawCircle(
                color = motionColor,
                radius = coreRadius * 0.5f,
                center = center
            )
        }
    }
}

private fun DrawScope.drawStaticRings(center: Offset, maxRadius: Float, color: Color) {
    listOf(0.35f, 0.6f, 0.85f, 1.0f).forEach { fraction ->
        drawCircle(
            color = color.copy(alpha = 0.08f),
            radius = maxRadius * fraction,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }
    // Cross-hairs
    drawLine(color.copy(alpha = 0.06f), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius), 1.dp.toPx())
    drawLine(color.copy(alpha = 0.06f), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y), 1.dp.toPx())
}

private fun DrawScope.drawPulseRing(center: Offset, maxRadius: Float, progress: Float, color: Color) {
    val radius = maxRadius * progress
    val alpha = (1f - progress).coerceIn(0f, 1f) * 0.6f
    if (alpha > 0f) {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = center,
            style = Stroke(width = (2f * (1f - progress) + 0.5f).dp.toPx())
        )
    }
}

private fun DrawScope.drawRadarSweep(center: Offset, maxRadius: Float, angle: Float, color: Color) {
    val sweepPath = Path().apply {
        moveTo(center.x, center.y)
        val startRad = Math.toRadians(angle.toDouble())
        val endRad = Math.toRadians((angle + 60).toDouble())
        lineTo(
            (center.x + maxRadius * cos(startRad)).toFloat(),
            (center.y + maxRadius * sin(startRad)).toFloat()
        )
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                center.x - maxRadius, center.y - maxRadius,
                center.x + maxRadius, center.y + maxRadius
            ),
            startAngleDegrees = angle,
            sweepAngleDegrees = 60f,
            forceMoveTo = false
        )
        close()
    }
    drawPath(
        path = sweepPath,
        brush = Brush.sweepGradient(
            colors = listOf(Color.Transparent, color.copy(alpha = 0.15f), Color.Transparent),
            center = center
        )
    )
}
