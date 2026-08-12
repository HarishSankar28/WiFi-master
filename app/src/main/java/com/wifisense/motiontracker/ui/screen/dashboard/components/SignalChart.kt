package com.wifisense.motiontracker.ui.screen.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wifisense.motiontracker.domain.model.MotionState
import com.wifisense.motiontracker.ui.theme.*

/**
 * Line chart displaying the last N RSSI readings over time.
 * Uses Canvas-based drawing for a clean, lightweight implementation
 * that avoids heavy chart library overhead.
 */
@Composable
fun SignalChart(
    rssiHistory: List<Float>,
    motionState: MotionState,
    modifier: Modifier = Modifier
) {
    val lineColor = motionState.toColor()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = NavyCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RSSI Signal History",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))

            if (rssiHistory.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "Collecting signal data…",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDisabled
                    )
                }
            } else {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val minRssi = rssiHistory.min()
                    val maxRssi = rssiHistory.max()
                    val range = (maxRssi - minRssi).let { if (it < 1f) 10f else it }
                    val stepX = w / (rssiHistory.size - 1).toFloat()

                    // Draw grid lines
                    listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = androidx.compose.ui.geometry.Offset(0f, h * frac),
                            end = androidx.compose.ui.geometry.Offset(w, h * frac),
                            strokeWidth = 1f
                        )
                    }

                    // Draw signal line
                    val path = androidx.compose.ui.graphics.Path()
                    rssiHistory.forEachIndexed { i, rssi ->
                        val x = i * stepX
                        val y = h - ((rssi - minRssi) / range * h)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.5f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )

                    // Fill area under line
                    val fillPath = androidx.compose.ui.graphics.Path().apply {
                        addPath(path)
                        lineTo((rssiHistory.size - 1) * stepX, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw current value dot
                    val lastX = (rssiHistory.size - 1) * stepX
                    val lastY = h - ((rssiHistory.last() - minRssi) / range * h)
                    drawCircle(color = lineColor, radius = 5f, center = androidx.compose.ui.geometry.Offset(lastX, lastY))
                    drawCircle(color = lineColor.copy(alpha = 0.3f), radius = 10f, center = androidx.compose.ui.geometry.Offset(lastX, lastY))
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (rssiHistory.isNotEmpty()) "${rssiHistory.last().toInt()} dBm" else "--",
                    style = MaterialTheme.typography.labelSmall,
                    color = lineColor
                )
                Text(
                    text = "${rssiHistory.size} samples",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled
                )
            }
        }
    }
}
