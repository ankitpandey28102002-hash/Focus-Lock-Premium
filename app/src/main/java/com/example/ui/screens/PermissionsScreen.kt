package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun PermissionsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isAccessibility by settingsViewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()
    val isUsageStats by settingsViewModel.isUsageStatsGranted.collectAsStateWithLifecycle()
    val isOverlay by settingsViewModel.isOverlayGranted.collectAsStateWithLifecycle()
    val isNotifications by settingsViewModel.isNotificationsGranted.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        settingsViewModel.refreshPermissionStates()
    }

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Permissions & Diagnostics",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { settingsViewModel.refreshPermissionStates() },
                        modifier = Modifier.testTag("refresh_permissions_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Status", tint = PurpleAccent)
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
            item {
                Text(
                    text = "FocusLock relies on standard Android system APIs to intercept distracting apps. Focus timers and analytics continue working even if blocking permissions are not enabled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }

            // Accessibility Service Card
            item {
                PermissionCard(
                    title = "Accessibility Service",
                    badge = "CRITICAL FOR REAL-TIME BLOCKING",
                    description = "Enables FocusLock to detect foreground window transitions and immediately deflect you away from locked applications during active sessions.",
                    isGranted = isAccessibility,
                    icon = Icons.Default.AccessibilityNew,
                    onGrantClick = { settingsViewModel.openAccessibilitySettings(context) },
                    testTag = "grant_accessibility_btn"
                )
            }

            // Usage Stats Card
            item {
                PermissionCard(
                    title = "Usage Access",
                    badge = "ACCURATE APP USAGE STATS",
                    description = "Allows FocusLock to inspect background launch frequencies to calculate which distracting apps attempt to interrupt you most.",
                    isGranted = isUsageStats,
                    icon = Icons.Default.QueryStats,
                    onGrantClick = { settingsViewModel.openUsageStatsSettings(context) },
                    testTag = "grant_usage_stats_btn"
                )
            }

            // Display Over Other Apps Card
            item {
                PermissionCard(
                    title = "Display Over Other Apps",
                    badge = "LOCK SCREEN OVERLAY",
                    description = "Allows FocusLock to render the motivational 'App Blocked' shield overlay directly over intercepted applications.",
                    isGranted = isOverlay,
                    icon = Icons.Default.Layers,
                    onGrantClick = { settingsViewModel.openOverlaySettings(context) },
                    testTag = "grant_overlay_btn"
                )
            }

            // Notifications Card
            item {
                PermissionCard(
                    title = "Notification Permission",
                    badge = "FOREGROUND TIMER TICKER",
                    description = "Required on Android 13+ to post persistent ongoing focus countdown timers in the status bar while the app is in the background.",
                    isGranted = isNotifications,
                    icon = Icons.Default.NotificationsActive,
                    onGrantClick = { settingsViewModel.openNotificationSettings(context) },
                    testTag = "grant_notifications_btn"
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    badge: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    onGrantClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal850),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) EmeraldSuccess.copy(alpha = 0.4f) else Charcoal700
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGranted) EmeraldSuccess.copy(alpha = 0.2f) else Charcoal700
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isGranted) EmeraldGlow else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGranted) EmeraldSuccess.copy(alpha = 0.2f) else Charcoal700
                ) {
                    Text(
                        text = if (isGranted) "GRANTED" else "REQUIRED",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) EmeraldGlow else AmberWarning,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(shape = RoundedCornerShape(4.dp), color = Charcoal900) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = PurpleAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag(testTag),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Grant in System Settings", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
