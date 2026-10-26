package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FocusProfileEntity
import com.example.data.model.ScheduleEntity
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun DashboardScreen(
    focusViewModel: FocusViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigate: (String) -> Unit
) {
    val todayMinutes by focusViewModel.todayFocusedMinutes.collectAsStateWithLifecycle()
    val dailyGoalMinutes by focusViewModel.dailyGoalMinutes.collectAsStateWithLifecycle()
    val streakDays by focusViewModel.streakDays.collectAsStateWithLifecycle()
    val completedSessions by focusViewModel.completedSessions.collectAsStateWithLifecycle()
    val profiles by focusViewModel.allProfiles.collectAsStateWithLifecycle()
    val schedules by focusViewModel.allSchedules.collectAsStateWithLifecycle()
    val serviceState by focusViewModel.serviceSessionState.collectAsStateWithLifecycle()
    val isDemoLoaded by settingsViewModel.isDemoDataLoaded.collectAsStateWithLifecycle()

    val progress = if (dailyGoalMinutes > 0) todayMinutes.toFloat() / dailyGoalMinutes else 0f
    val hours = todayMinutes / 60
    val mins = todayMinutes % 60
    val focusedTimeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

    val goalHours = dailyGoalMinutes / 60
    val goalMins = dailyGoalMinutes % 60
    val goalTimeStr = if (goalHours > 0) "${goalHours}h ${goalMins}m" else "${goalMins}m"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Charcoal950)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FocusLock",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        UnlockedProBadge()
                    }
                    Text(
                        text = "Unbreakable Productivity",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row {
                    IconButton(
                        onClick = { onNavigate(Screen.DevTesting.route) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Charcoal850)
                            .testTag("dashboard_dev_test_button")
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "Testing Mode",
                            tint = PurpleAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onNavigate(Screen.Preferences.route) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Charcoal850)
                            .testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Preferences",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Demo Data Notice Banner if active
        if (isDemoLoaded) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Demo Mode Active — Showing sample session and blocking logs",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberWarning
                        )
                    }
                }
            }
        }

        // Active Session Card (if running)
        if (serviceState.isRunning) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable { onNavigate(Screen.ActiveSession.route) }
                        .testTag("active_session_card"),
                    colors = CardDefaults.cardColors(containerColor = PurpleDark.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PurpleAccent),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (serviceState.isPaused) AmberWarning else EmeraldSuccess)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (serviceState.isPaused) "SESSION PAUSED" else "SESSION IN PROGRESS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (serviceState.isPaused) AmberWarning else EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = serviceState.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        val minsLeft = serviceState.remainingSeconds / 60
                        val secsLeft = serviceState.remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", minsLeft, secsLeft),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent
                        )
                    }
                }
            }
        }

        // Center Progress Ring & Daily Goal
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Charcoal900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FocusProgressRing(
                        progress = progress,
                        centerText = focusedTimeStr,
                        subText = "Goal: $goalTimeStr",
                        size = 200.dp,
                        isPulsing = serviceState.isRunning && !serviceState.isPaused
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Blocking Status Live Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (serviceState.isRunning) EmeraldSuccess.copy(alpha = 0.15f) else Charcoal800,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (serviceState.isRunning) EmeraldSuccess.copy(alpha = 0.4f) else Charcoal600
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (serviceState.isRunning) Icons.Default.Shield else Icons.Default.LockClock,
                                contentDescription = null,
                                tint = if (serviceState.isRunning) EmeraldGlow else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (serviceState.isRunning) "Distraction Shield Active" else "Blocking Ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (serviceState.isRunning) EmeraldGlow else TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Stats Row: Streak & Completed Sessions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "CURRENT STREAK",
                    value = "$streakDays Days",
                    subtitle = if (streakDays > 0) "Keep the momentum going!" else "Start a session today",
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = AmberWarning,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "COMPLETED",
                    value = "${completedSessions.size} Sessions",
                    subtitle = "All-time focus history",
                    icon = Icons.Default.CheckCircle,
                    iconTint = EmeraldSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.History.route) }
                )
            }
        }

        // Quick Focus Launch Presets
        item {
            SectionHeader(
                title = "Quick Focus",
                actionLabel = "Custom Timer",
                onAction = { onNavigate(Screen.TimerSetup.route) }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(15, 25, 45, 60).forEach { minsPreset ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                focusViewModel.setDuration(minsPreset)
                                focusViewModel.setTitle("Quick Focus ($minsPreset min)")
                                onNavigate(Screen.TimerSetup.route)
                            }
                            .testTag("quick_focus_${minsPreset}_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Charcoal850),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$minsPreset",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PurpleAccent
                            )
                            Text(
                                text = "MIN",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Focus Profiles Carousel
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "Focus Profiles",
                actionLabel = "All Modes",
                onAction = { onNavigate(Screen.FocusModeSelector.route) }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = profiles, key = { it.id }) { profile ->
                    ProfileCard(profile = profile) {
                        focusViewModel.applyProfile(profile)
                        onNavigate(Screen.TimerSetup.route)
                    }
                }
            }
        }

        // Major Feature Shortcuts
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Focus Toolkit")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureShortcutCard(
                        title = "Block Lists",
                        subtitle = "Manage locked apps",
                        icon = Icons.Default.Shield,
                        tint = PurpleAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.BlockListManager.route) }
                    )
                    FeatureShortcutCard(
                        title = "Strict Mode",
                        subtitle = "Distraction shields",
                        icon = Icons.Default.Lock,
                        tint = RoseStrict,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.StrictModeConfig.route) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureShortcutCard(
                        title = "Schedules",
                        subtitle = "Automated routines",
                        icon = Icons.Default.Schedule,
                        tint = IndigoAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.ScheduleManager.route) }
                    )
                    FeatureShortcutCard(
                        title = "Insights",
                        subtitle = "Reports & trends",
                        icon = Icons.Default.BarChart,
                        tint = CyanFocus,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.Insights.route) }
                    )
                }
            }
        }

        // Bottom Start Big FAB
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Button(
                    onClick = {
                        if (serviceState.isRunning) {
                            onNavigate(Screen.ActiveSession.route)
                        } else {
                            onNavigate(Screen.TimerSetup.route)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("dashboard_start_focus_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(
                        if (serviceState.isRunning) Icons.Default.PlayArrow else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (serviceState.isRunning) "View Active Focus Session" else "Start Focus Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: FocusProfileEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal850),
        border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = PurpleAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (profile.isStrict) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RoseStrict.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "STRICT",
                            color = RoseStrict,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${profile.defaultDurationMinutes} min session",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun FeatureShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal850),
        border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
