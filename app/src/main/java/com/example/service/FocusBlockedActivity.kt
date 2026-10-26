package com.example.service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainActivity
import com.example.ui.theme.*

class FocusBlockedActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_APP = "EXTRA_BLOCKED_APP"
        const val EXTRA_BLOCKED_PKG = "EXTRA_BLOCKED_PKG"
        const val EXTRA_REASON = "EXTRA_REASON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appName = intent.getStringExtra(EXTRA_BLOCKED_APP) ?: "This Application"

        setContent {
            FocusLockTheme(darkTheme = true) {
                val sessionState by FocusSessionService.sessionState.collectAsStateWithLifecycle()
                var showEmergencyDialog by remember { mutableStateOf(false) }

                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Charcoal950
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E1738),
                                        Charcoal950,
                                        Color(0xFF0F0C1B)
                                    )
                                )
                            )
                            .systemBarsPadding()
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Tag
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PurplePrimary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleAccent.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = PurpleAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "FOCUS LOCK ACTIVE",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PurpleAccent,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }

                            // Center Content
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(PurplePrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(PurplePrimary, PurpleDark)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Shield,
                                            contentDescription = "Shield Locked",
                                            tint = Color.White,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                Text(
                                    text = "$appName is Locked",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Your flow state is protected. Stay intentional and keep your focus intact.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Remaining Time Pill
                                val remainingMins = sessionState.remainingSeconds / 60
                                val remainingSecs = sessionState.remainingSeconds % 60
                                val timeText = String.format("%02d:%02d", remainingMins, remainingSecs)

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "TIME REMAINING IN SESSION",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = timeText,
                                            style = MaterialTheme.typography.displayMedium,
                                            color = PurpleAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = sessionState.title.ifBlank { "Deep Work Sprint" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Motivational quote
                                Text(
                                    text = "“Discipline is remembering what you really want.”",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            // Bottom Buttons
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(this@FocusBlockedActivity, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                        startActivity(intent)
                                        finish()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("return_to_focus_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PurplePrimary
                                    )
                                ) {
                                    Text(
                                        text = "Return to Focus",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                TextButton(
                                    onClick = {
                                        if (sessionState.isStrict) {
                                            showEmergencyDialog = true
                                        } else {
                                            finish()
                                        }
                                    },
                                    modifier = Modifier.testTag("dismiss_block_screen_button")
                                ) {
                                    Text(
                                        text = if (sessionState.isStrict) "Strict Mode Active (Locked)" else "Dismiss Overlay",
                                        color = if (sessionState.isStrict) RoseStrict else TextSecondary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        if (showEmergencyDialog) {
                            AlertDialog(
                                onDismissRequest = { showEmergencyDialog = false },
                                title = { Text("Strict Focus Shield Active") },
                                text = {
                                    Text("This session was locked with Strict Mode enabled. Emergency phone calls and system settings remain accessible, but distracting apps remain locked until the timer expires.")
                                },
                                confirmButton = {
                                    TextButton(onClick = { showEmergencyDialog = false }) {
                                        Text("Understood")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
