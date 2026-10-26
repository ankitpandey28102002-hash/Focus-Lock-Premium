package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.components.UnlockedProBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun ProfilePreferencesScreen(
    settingsViewModel: SettingsViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val dailyGoalMinutes by settingsViewModel.dailyGoalMinutes.collectAsStateWithLifecycle()

    val goalHours = dailyGoalMinutes / 60
    val goalRemainingMins = dailyGoalMinutes % 60
    val goalStr = if (goalHours > 0) "${goalHours}h ${goalRemainingMins}m" else "${goalRemainingMins}m"

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Preferences & Profile",
                onBack = onBack,
                actions = { UnlockedProBadge() }
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
            // Daily Focus Goal Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
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
                                text = "DAILY FOCUS TARGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = goalStr,
                                style = MaterialTheme.typography.titleMedium,
                                color = PurpleAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Slider(
                            value = dailyGoalMinutes.toFloat(),
                            onValueChange = { settingsViewModel.setDailyGoal(it.toInt()) },
                            valueRange = 30f..360f,
                            steps = 32,
                            colors = SliderDefaults.colors(
                                thumbColor = PurpleAccent,
                                activeTrackColor = PurplePrimary,
                                inactiveTrackColor = Charcoal700
                            ),
                            modifier = Modifier.testTag("daily_goal_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(60, 120, 180, 240).forEach { mins ->
                                val isSelected = dailyGoalMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { settingsViewModel.setDailyGoal(mins) },
                                    label = { Text("${mins / 60}h Goal") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = TextPrimary,
                                        containerColor = Charcoal900,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Quick Nav Links
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column {
                        PreferenceNavLink(
                            title = "Theme & Appearance",
                            subtitle = "Dark Charcoal Pro, Midnight Violet, Light",
                            icon = Icons.Default.Palette,
                            onClick = { onNavigate(Screen.ThemeSettings.route) }
                        )
                        HorizontalDivider(color = Charcoal700)
                        PreferenceNavLink(
                            title = "Notifications & Audio",
                            subtitle = "Sound chimes, vibration pulses, reminders",
                            icon = Icons.Default.Notifications,
                            onClick = { onNavigate(Screen.NotificationSettings.route) }
                        )
                        HorizontalDivider(color = Charcoal700)
                        PreferenceNavLink(
                            title = "System Permissions & Status",
                            subtitle = "Accessibility, usage stats, overlay diagnostics",
                            icon = Icons.Default.Security,
                            onClick = { onNavigate(Screen.Permissions.route) }
                        )
                        HorizontalDivider(color = Charcoal700)
                        PreferenceNavLink(
                            title = "Backup & Data Export",
                            subtitle = "Copy JSON backup or clear local records",
                            icon = Icons.Default.Storage,
                            onClick = { onNavigate(Screen.BackupRestore.route) }
                        )
                    }
                }
            }

            // Privacy Guarantee Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Zero Tracking • 100% Private",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All timers, block lists, and logs remain locally on this device. No remote servers, accounts, or analytics tracking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Developer Testing Mode Button
            item {
                OutlinedButton(
                    onClick = { onNavigate(Screen.DevTesting.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("open_dev_testing_btn"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal600)
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, tint = PurpleAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Developer Testing & Diagnostic Tools", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PreferenceNavLink(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
