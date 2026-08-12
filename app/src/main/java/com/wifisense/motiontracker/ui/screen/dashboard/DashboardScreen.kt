package com.wifisense.motiontracker.ui.screen.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.wifisense.motiontracker.domain.model.MotionState
import com.wifisense.motiontracker.ui.common.PermissionScreen
import com.wifisense.motiontracker.ui.screen.dashboard.components.*
import com.wifisense.motiontracker.ui.theme.*
import com.wifisense.motiontracker.util.formatVariance
import com.wifisense.motiontracker.util.normalizeVariance
import com.wifisense.motiontracker.util.toMinuteSeconds
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    onNavigateToCalibration: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Required permissions
    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    if (!permissionsState.allPermissionsGranted) {
        PermissionScreen(
            permissionsState = permissionsState,
            paddingValues = paddingValues
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(NavyDeep, NavySurface))
            )
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "WiFi Motion Tracker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary, fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "UBIQCOM UB5021 GVWD",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                IconButton(onClick = onNavigateToCalibration) {
                    Icon(Icons.Filled.Tune, "Calibrate", tint = CyanPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Router Status Card ───────────────────────────────────────────
            RouterStatusCard(
                ssid = uiState.routerSsid,
                bssid = uiState.routerBssid,
                rssi = uiState.rawRssi,
                frequency = 2412,
                isConnected = uiState.routerSsid.isNotEmpty()
            )

            Spacer(Modifier.height(20.dp))

            // ── Radar + Motion State ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(NavyCard),
                contentAlignment = Alignment.Center
            ) {
                MotionRadar(
                    motionState = uiState.motionState,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = uiState.motionState,
                        transitionSpec = {
                            fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                        }, label = "motion_state_text"
                    ) { state ->
                        Text(
                            text = state.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = state.toColor(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                    if (uiState.isMonitoring) {
                        Text(
                            text = uiState.sessionElapsedMs.toMinuteSeconds(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary, fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // State description
            AnimatedContent(
                targetState = uiState.motionState.description,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "state_desc"
            ) { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary, textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Variance Intensity Bar ───────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                color = NavyCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Motion Intensity", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text(
                            uiState.rssiVariance.formatVariance(),
                            style = MaterialTheme.typography.labelMedium,
                            color = uiState.motionState.toColor()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    val progress by animateFloatAsState(
                        targetValue = uiState.rssiVariance.normalizeVariance(),
                        animationSpec = tween(600), label = "intensity_progress"
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = uiState.motionState.toColor(),
                        trackColor = NavyElevated
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Still", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                        Text("Active", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                        Text("Vigorous", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── RSSI Signal Chart ────────────────────────────────────────────
            SignalChart(
                rssiHistory = uiState.rssiHistory,
                motionState = uiState.motionState
            )

            Spacer(Modifier.height(24.dp))

            // ── Start / Stop Button ──────────────────────────────────────────
            Button(
                onClick = { viewModel.toggleMonitoring() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isMonitoring) Error else CyanPrimary,
                    contentColor = NavyDeep
                )
            ) {
                Icon(
                    imageVector = if (uiState.isMonitoring) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (uiState.isMonitoring) "Stop Monitoring" else "Start Monitoring",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Error message
            uiState.errorMessage?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall.copy(color = Error),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
