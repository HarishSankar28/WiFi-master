package com.wifisense.motiontracker.ui.screen.calibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wifisense.motiontracker.ui.theme.*
import com.wifisense.motiontracker.util.formatVariance

@Composable
fun CalibrationScreen(
    onCalibrationComplete: () -> Unit,
    viewModel: CalibrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyDeep, NavySurface)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon header
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(NavyCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isComplete) Icons.Filled.CheckCircle else Icons.Filled.Radar,
                    contentDescription = null,
                    tint = if (uiState.isComplete) MotionMinor else CyanPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = if (uiState.isComplete) "Calibration Complete!" else "Baseline Calibration",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary, fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = when {
                    uiState.isComplete -> "Baseline RSSI noise floor established. You can now use motion tracking."
                    uiState.isCalibrating -> "Measuring empty-room background signal variance. Please step out of the room."
                    else -> "Please step out of the room or remain completely still for 30 seconds to calibrate the baseline noise floor."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary, textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(32.dp))

            // Calibration Progress Box
            if (uiState.isCalibrating || uiState.isComplete) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = NavyCard
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.isCalibrating) {
                            Text(
                                text = "${uiState.remainingSeconds}s remaining",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    color = CyanPrimary, fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { uiState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyanPrimary,
                                trackColor = NavyElevated
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Collected ${uiState.sampleCount} samples",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled)
                            )
                        } else if (uiState.isComplete) {
                            Text(
                                text = "Baseline Noise Floor",
                                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = uiState.computedBaseline.formatVariance(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MotionMinor, fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            } else {
                // Info Card before start
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = NavyCard
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, null, tint = CyanPrimary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Ensures accurate motion sensing by filtering out ambient WiFi fluctuations.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = {
                    if (uiState.isComplete) {
                        onCalibrationComplete()
                    } else if (!uiState.isCalibrating) {
                        viewModel.startCalibration()
                    }
                },
                enabled = !uiState.isCalibrating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isComplete) MotionMinor else CyanPrimary,
                    contentColor = NavyDeep
                )
            ) {
                Text(
                    text = when {
                        uiState.isComplete -> "Done"
                        uiState.isCalibrating -> "Calibrating..."
                        else -> "Start 30s Calibration"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            uiState.errorMessage?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall.copy(color = Error),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
