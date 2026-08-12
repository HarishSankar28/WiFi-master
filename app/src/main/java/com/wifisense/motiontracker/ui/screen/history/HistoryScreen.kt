package com.wifisense.motiontracker.ui.screen.history

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wifisense.motiontracker.domain.model.ActivitySession
import com.wifisense.motiontracker.domain.model.MotionState
import com.wifisense.motiontracker.ui.theme.*
import com.wifisense.motiontracker.util.formatVariance
import com.wifisense.motiontracker.util.toFormattedDate
import com.wifisense.motiontracker.util.toFormattedTime

@Composable
fun HistoryScreen(
    paddingValues: PaddingValues,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyDeep, NavySurface)))
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Activity History",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary, fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "${uiState.totalSessions} sessions recorded",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                if (uiState.sessions.isNotEmpty()) {
                    IconButton(onClick = { showDeleteAllDialog = true }) {
                        Icon(Icons.Filled.DeleteSweep, "Clear all", tint = Error)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Summary Stats ────────────────────────────────────────────────
            if (uiState.sessions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Total Sessions",
                        value = "${uiState.totalSessions}",
                        icon = Icons.Filled.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Active Minutes",
                        value = "${uiState.totalActiveMinutes}m",
                        icon = Icons.Filled.DirectionsWalk,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Session List ─────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CyanPrimary)
                    }
                }
                uiState.sessions.isEmpty() -> {
                    EmptyHistoryView()
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.sessions,
                            key = { it.id }
                        ) { session ->
                            SessionCard(
                                session = session,
                                onDelete = { viewModel.deleteSession(session) }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }

    // ── Delete All Confirmation Dialog ───────────────────────────────────────
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            containerColor = NavyCard,
            title = {
                Text("Clear All History", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will permanently delete all ${uiState.totalSessions} sessions.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllSessions()
                    showDeleteAllDialog = false
                }) {
                    Text("Delete All", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ── Session Card ─────────────────────────────────────────────────────────────

@Composable
private fun SessionCard(
    session: ActivitySession,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val stateColor = session.dominantState.toColor()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = NavyCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: date/time + state badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.startTime.toFormattedDate(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextPrimary, fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "${session.startTime.toFormattedTime()} — ${session.endTime.toFormattedTime()}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        fontSize = 11.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = stateColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = session.dominantState.label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = stateColor, fontWeight = FontWeight.SemiBold
                            ),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete, "Delete",
                            tint = TextDisabled, modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = NavyElevated)
            Spacer(Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SessionStat(label = "Duration", value = session.durationFormatted)
                SessionStat(label = "Active", value = "${session.activeMinutes}m")
                SessionStat(label = "Peak Var.", value = session.peakVariance.formatVariance())
                SessionStat(label = "Events", value = "${session.eventCount}")
            }

            Spacer(Modifier.height(10.dp))

            // Activity bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Activity",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled),
                    modifier = Modifier.width(52.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(NavyElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(session.activityPercent / 100f)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MotionStationary, stateColor)
                                )
                            )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "${session.activityPercent}%",
                    style = MaterialTheme.typography.labelSmall.copy(color = stateColor),
                    fontSize = 11.sp
                )
            }

            // Router info
            Spacer(Modifier.height(6.dp))
            Text(
                text = "📡 ${session.routerSsid}",
                style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled),
                fontSize = 10.sp
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = NavyCard,
            title = { Text("Delete Session", color = TextPrimary) },
            text = { Text("Remove this session from history?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SessionStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                color = CyanPrimary, fontWeight = FontWeight.SemiBold
            ),
            fontSize = 13.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = NavyCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary, fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.History, "No history",
                tint = TextDisabled,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No sessions yet",
                style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Start monitoring to record your first session",
                style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled)
            )
        }
    }
}
