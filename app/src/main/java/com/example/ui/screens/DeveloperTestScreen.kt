package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.FocusSessionService
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.FocusViewModel
import com.example.ui.viewmodel.ScheduleViewModel
import com.example.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DeveloperTestScreen(
    focusViewModel: FocusViewModel,
    settingsViewModel: SettingsViewModel,
    analyticsViewModel: AnalyticsViewModel,
    scheduleViewModel: ScheduleViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val serviceState by focusViewModel.serviceSessionState.collectAsStateWithLifecycle()
    val isAccessibility by settingsViewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()
    val isUsageStats by settingsViewModel.isUsageStatsGranted.collectAsStateWithLifecycle()
    val isOverlay by settingsViewModel.isOverlayGranted.collectAsStateWithLifecycle()
    val isNotifications by settingsViewModel.isNotificationsGranted.collectAsStateWithLifecycle()
    val isDemoLoaded by settingsViewModel.isDemoDataLoaded.collectAsStateWithLifecycle()
    val recentAuditLogs by analyticsViewModel.recentAuditLogs.collectAsStateWithLifecycle()
    val blockLists by focusViewModel.allBlockLists.collectAsStateWithLifecycle()
    val schedules by scheduleViewModel.allSchedules.collectAsStateWithLifecycle()

    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Developer Testing & Diagnostics",
                onBack = onBack
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
            // Live Blocking Engine Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BLOCKING ENGINE STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (serviceState.isRunning) EmeraldSuccess.copy(alpha = 0.2f) else Charcoal700
                            ) {
                                Text(
                                    text = if (serviceState.isRunning) "ACTIVE ENFORCING" else "IDLE / STANDBY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (serviceState.isRunning) EmeraldGlow else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("• Session Running: ${serviceState.isRunning}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text("• Active Session: ${serviceState.title.ifBlank { "None" }}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("• Strict Lock: ${serviceState.isStrict}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("• Locked Packages Count: ${serviceState.blockedPackages.size}", style = MaterialTheme.typography.bodySmall, color = PurpleAccent)
                        Text("• Remaining Time: ${serviceState.remainingSeconds}s", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            // Permissions Quick Diagnostic
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SYSTEM PERMISSIONS TELEMETRY", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        DiagnosticStatusRow("Accessibility Service (Real-time blocking)", isAccessibility)
                        DiagnosticStatusRow("Usage Stats (App launch tracking)", isUsageStats)
                        DiagnosticStatusRow("Overlay Permission (Lock screen shield)", isOverlay)
                        DiagnosticStatusRow("Notifications (Status bar timer)", isNotifications)
                    }
                }
            }

            // Quick Testing Actions (Seed Demo Data, Test 1m Session, Reset Data)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("SIMULATION & TESTING CONTROLS", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)

                        // Seed Demo Data Button
                        Button(
                            onClick = {
                                settingsViewModel.seedDemoData()
                                Toast.makeText(context, "Seeded realistic focus history and block logs", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("seed_demo_data_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Icon(Icons.Default.Dataset, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isDemoLoaded) "Re-seed Demo Data (Active)" else "Seed Realistic Demo Data")
                        }

                        // Test Quick 1-Min Session
                        OutlinedButton(
                            onClick = {
                                focusViewModel.setDuration(1)
                                focusViewModel.setTitle("Test 1-Minute Focus Run")
                                focusViewModel.setStrictMode(false)
                                focusViewModel.startSession()
                                Toast.makeText(context, "Started 1-min test session", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("test_quick_session_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch 1-Min Test Session", color = TextPrimary)
                        }

                        // Clear Local Test Data
                        OutlinedButton(
                            onClick = {
                                settingsViewModel.clearAllData()
                                Toast.makeText(context, "Local test data reset", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("reset_test_data_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseStrict)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = RoseStrict, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Local Test Data", color = RoseStrict)
                        }
                    }
                }
            }

            // Real-Time Interception Audit Log
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("RECENT INTERCEPT AUDIT LOGS (${recentAuditLogs.size})", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.History, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (recentAuditLogs.isEmpty()) {
                            Text("No apps have been blocked yet. Active logs will stream here in real-time.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        } else {
                            recentAuditLogs.take(8).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = log.appName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${log.packageName} • ${log.blockReason}", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                    }
                                    Text(text = sdf.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall, color = PurpleAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticStatusRow(label: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (isOk) EmeraldSuccess.copy(alpha = 0.2f) else RoseStrict.copy(alpha = 0.2f)
        ) {
            Text(
                text = if (isOk) "ACTIVE" else "DISABLED",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOk) EmeraldGlow else RoseStrict,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
