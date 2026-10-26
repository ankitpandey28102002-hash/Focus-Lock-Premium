package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.components.FocusProgressRing
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import kotlinx.coroutines.delay

@Composable
fun ActiveSessionScreen(
    focusViewModel: FocusViewModel,
    onNavigate: (String) -> Unit,
    onBackToDashboard: () -> Unit
) {
    val serviceState by focusViewModel.serviceSessionState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    var strictCooldownRemaining by remember { mutableStateOf(0) }

    // Intercept hardware back button during active session
    BackHandler {
        onBackToDashboard()
    }

    // Strict cooldown ticker if active
    LaunchedEffect(strictCooldownRemaining) {
        if (strictCooldownRemaining > 0) {
            delay(1000L)
            strictCooldownRemaining -= 1
        }
    }

    val totalSecs = serviceState.durationMinutes * 60
    val progress = if (totalSecs > 0) {
        (totalSecs - serviceState.remainingSeconds).toFloat() / totalSecs
    } else 0f

    val mins = serviceState.remainingSeconds / 60
    val secs = serviceState.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = serviceState.title.ifBlank { "Focus Lock" },
                onBack = onBackToDashboard,
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (serviceState.isStrict) RoseStrict.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (serviceState.isStrict) "STRICT LOCK" else "FOCUSED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (serviceState.isStrict) RoseStrict else EmeraldGlow,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        },
        containerColor = Charcoal950
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Shield Tag
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Charcoal850,
                border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = PurpleAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${serviceState.blockedPackages.size} Apps Locked in Background",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                }
            }

            // Center Ring Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FocusProgressRing(
                    progress = progress,
                    centerText = timeFormatted,
                    subText = if (serviceState.isPaused) "SESSION PAUSED" else "${serviceState.durationMinutes}m Goal",
                    size = 250.dp,
                    strokeWidth = 18.dp,
                    ringColor1 = if (serviceState.isStrict) RoseStrict else PurpleAccent,
                    ringColor2 = if (serviceState.isStrict) Color(0xFF9F1239) else PurpleDark,
                    isPulsing = !serviceState.isPaused
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = if (serviceState.isPaused) "Take a gentle breath, then resume." else "“Deep work produces extraordinary value.”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Bottom Timer Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pause / Resume (disabled if strict mode)
                    if (!serviceState.isStrict) {
                        OutlinedButton(
                            onClick = {
                                if (serviceState.isPaused) {
                                    focusViewModel.resumeSession()
                                } else {
                                    focusViewModel.pauseSession()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("pause_resume_session_btn"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal600)
                        ) {
                            Icon(
                                if (serviceState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (serviceState.isPaused) "Resume" else "Pause",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Finish / Complete Button
                    Button(
                        onClick = {
                            if (serviceState.isStrict) {
                                strictCooldownRemaining = 10
                                showExitDialog = true
                            } else {
                                showExitDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("end_session_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (serviceState.isStrict) Charcoal800 else PurplePrimary
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (serviceState.isStrict) "Emergency Exit" else "End Session",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onBackToDashboard,
                    modifier = Modifier.testTag("minimize_to_dashboard_btn")
                ) {
                    Text(
                        text = "Minimize (Keep timer running in background)",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }

    // Exit / Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = if (serviceState.isStrict) "Strict Mode Emergency Exit" else "Finish Focus Session?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    if (serviceState.isStrict) {
                        Text(
                            text = "Strict mode was engaged to hold you accountable. Are you sure you need to exit?",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (strictCooldownRemaining > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Cooldown: Exit unlocks in ${strictCooldownRemaining}s...",
                                color = AmberWarning,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    } else {
                        Text(
                            text = "Do you want to complete your session now or abandon it?",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        focusViewModel.finishSession(abandoned = false)
                        onNavigate(Screen.SessionSummary.route)
                    },
                    enabled = !serviceState.isStrict || strictCooldownRemaining == 0,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier.testTag("confirm_finish_session_btn")
                ) {
                    Text("Complete Session")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        focusViewModel.finishSession(abandoned = true)
                        onNavigate(Screen.SessionSummary.route)
                    },
                    enabled = !serviceState.isStrict || strictCooldownRemaining == 0
                ) {
                    Text("Abandon Early", color = RoseStrict)
                }
            },
            containerColor = Charcoal900
        )
    }
}
