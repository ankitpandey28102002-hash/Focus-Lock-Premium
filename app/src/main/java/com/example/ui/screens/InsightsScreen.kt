package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.components.MetricCard
import com.example.ui.components.UnlockedProBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AnalyticsViewModel

@Composable
fun InsightsScreen(
    analyticsViewModel: AnalyticsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val totalMins by analyticsViewModel.totalFocusedMinutes.collectAsStateWithLifecycle()
    val completedCount by analyticsViewModel.completedCount.collectAsStateWithLifecycle()
    val abandonedCount by analyticsViewModel.abandonedCount.collectAsStateWithLifecycle()
    val completionRate by analyticsViewModel.completionRate.collectAsStateWithLifecycle()
    val streakDays by analyticsViewModel.streakDays.collectAsStateWithLifecycle()
    val last7Days by analyticsViewModel.last7DaysData.collectAsStateWithLifecycle()
    val topBlockedApps by analyticsViewModel.topBlockedApps.collectAsStateWithLifecycle()
    val selectedRangeIdx by analyticsViewModel.selectedRangeIndex.collectAsStateWithLifecycle()

    var showExportDialog by remember { mutableStateOf(false) }

    val hours = totalMins / 60
    val mins = totalMins % 60
    val totalTimeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Focus Insights & Analytics",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_analytics_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Data", tint = PurpleAccent)
                    }
                }
            )
        },
        containerColor = Charcoal950
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Range Selector (7 Days, 30 Days, All Time)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Last 7 Days", "Last 30 Days", "All Time").forEachIndexed { index, label ->
                        val isSelected = selectedRangeIdx == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { analyticsViewModel.setSelectedRange(index) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePrimary,
                                selectedLabelColor = TextPrimary,
                                containerColor = Charcoal850,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Summary Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "TOTAL TIME",
                        value = totalTimeStr,
                        subtitle = "Cumulative focus",
                        icon = Icons.Default.Timer,
                        iconTint = PurpleAccent,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "SUCCESS RATE",
                        value = "$completionRate%",
                        subtitle = "$completedCount of ${completedCount + abandonedCount} completed",
                        icon = Icons.Default.CheckCircle,
                        iconTint = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 7-Day Focus Visual Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "DAILY FOCUS TRENDS (LAST 7 DAYS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        val maxMinutes = (last7Days.maxOfOrNull { it.minutes } ?: 60).coerceAtLeast(60)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last7Days.forEach { dayData ->
                                val barHeightFraction = (dayData.minutes.toFloat() / maxMinutes).coerceIn(0.08f, 1.0f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${dayData.minutes}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (dayData.minutes > 0) PurpleAccent else TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(barHeightFraction)
                                            .width(18.dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (dayData.minutes > 0) PurplePrimary else Charcoal700
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = dayData.dayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top Blocked Apps Leaderboard
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MOST FREQUENTLY BLOCKED APPS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (topBlockedApps.isEmpty()) {
                            Text(
                                text = "No block attempts recorded yet. Your focus shields are active and watching.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        } else {
                            topBlockedApps.forEachIndexed { index, stat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PurpleAccent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(26.dp)
                                        )
                                        Text(
                                            text = stat.appName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Charcoal700
                                    ) {
                                        Text(
                                            text = "${stat.count} blocks intercepted",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldGlow,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Local Focus Report", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Export your private focus session records. All data remains exclusively on your device.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val csv = analyticsViewModel.generateCsvExport()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("FocusLock CSV Export", csv))
                        Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Copy CSV")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val json = analyticsViewModel.generateJsonExport()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("FocusLock JSON Export", json))
                        Toast.makeText(context, "JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Text("Copy JSON", color = TextPrimary)
                }
            },
            containerColor = Charcoal900
        )
    }
}
