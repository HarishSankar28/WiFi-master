package com.wifisense.motiontracker.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wifisense.motiontracker.ui.theme.*
import com.wifisense.motiontracker.util.Constants
import com.wifisense.motiontracker.util.formatVariance

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    onNavigateToCalibration: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyDeep, NavySurface)))
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Text(
                "Settings & Config",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary, fontWeight = FontWeight.Bold
                )
            )
            Text(
                "Tune sensitivity and router parameters",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(Modifier.height(16.dp))

            // ── Section 1: Router Connection ──────────────────────────────────────
            SettingsSectionHeader("Monitored Router")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = NavyCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Router, null, tint = CyanPrimary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (uiState.pinnedSsid.isNotEmpty()) uiState.pinnedSsid else "Auto-Detect (UBIQCOM)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextPrimary, fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                if (uiState.pinnedBssid.isNotEmpty()) uiState.pinnedBssid else "BSSID: Any matching AP",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "App targets UBIQCOM UB5021 GVWD router signals for RSSI variance tracking.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 2: Calibration ────────────────────────────────────────────
            SettingsSectionHeader("Environment Baseline")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = NavyCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Empty Room Calibration",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextPrimary, fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                if (uiState.calibrationDone)
                                    "Baseline noise: ${uiState.baselineVariance.formatVariance()}"
                                else
                                    "Not calibrated yet",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.calibrationDone) MotionMinor else Warning
                                ),
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = onNavigateToCalibration,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = NavyDeep
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (uiState.calibrationDone) "Recalibrate" else "Start", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 3: Motion Sensitivity ──────────────────────────────────────
            SettingsSectionHeader("Sensing Parameters")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = NavyCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Sensitivity Level",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextPrimary, fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            "Level ${uiState.sensitivityLevel} / 5",
                            style = MaterialTheme.typography.labelMedium.copy(color = CyanPrimary)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = uiState.sensitivityLevel.toFloat(),
                        onValueChange = { viewModel.setSensitivity(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary,
                            inactiveTrackColor = NavyElevated
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Low (Robust)", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                        Text("Medium", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                        Text("High (Sensitive)", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 4: System & Service ───────────────────────────────────────
            SettingsSectionHeader("Background Execution")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = NavyCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    SettingsSwitchRow(
                        title = "Background Monitoring",
                        subtitle = "Keep tracking motion when app is minimized",
                        icon = Icons.Filled.CloudSync,
                        checked = uiState.backgroundEnabled,
                        onCheckedChange = { viewModel.setBackgroundEnabled(it) }
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = NavyElevated)
                    Spacer(Modifier.height(12.dp))
                    SettingsSwitchRow(
                        title = "Live Status Notification",
                        subtitle = "Show current state in status bar",
                        icon = Icons.Filled.Notifications,
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = CyanPrimary, fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = CyanPrimary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary, fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    fontSize = 11.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NavyDeep,
                checkedTrackColor = CyanPrimary,
                uncheckedThumbColor = TextDisabled,
                uncheckedTrackColor = NavyElevated
            )
        )
    }
}
