package com.wifisense.motiontracker.ui.screen.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifisense.motiontracker.ui.theme.*
import com.wifisense.motiontracker.util.rssiToQualityLabel
import com.wifisense.motiontracker.util.rssiToQualityPercent

/**
 * Card showing UBIQCOM router connection status, SSID, signal strength, and band.
 */
@Composable
fun RouterStatusCard(
    ssid: String,
    bssid: String,
    rssi: Int,
    frequency: Int,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val band = if (frequency > 4000) "5 GHz" else "2.4 GHz"
    val quality = rssi.rssiToQualityPercent()
    val qualityLabel = rssi.rssiToQualityLabel()
    val signalIcon = when {
        quality >= 80 -> Icons.Filled.SignalWifi4Bar
        quality >= 60 -> Icons.Filled.Wifi
        quality >= 40 -> Icons.Filled.SignalWifi0Bar
        else          -> Icons.Filled.SignalWifiStatusbarConnectedNoInternet4
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = NavyCard,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: router icon + SSID info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Router,
                    contentDescription = "Router",
                    tint = if (isConnected) CyanPrimary else TextDisabled,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (ssid.isNotEmpty()) ssid else "UBIQCOM Router",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = if (isConnected) "Connected · $band" else "Scanning…",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        fontSize = 11.sp
                    )
                    if (bssid.isNotEmpty()) {
                        Text(
                            text = bssid.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Right: signal strength
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = signalIcon,
                    contentDescription = "Signal",
                    tint = when {
                        quality >= 70 -> MotionStationary
                        quality >= 40 -> MotionModerate
                        else          -> MotionHeavy
                    },
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${rssi} dBm",
                    style = MaterialTheme.typography.labelMedium.copy(color = CyanPrimary),
                    fontSize = 11.sp
                )
                Text(
                    text = qualityLabel,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    fontSize = 10.sp
                )
            }
        }
    }
}
