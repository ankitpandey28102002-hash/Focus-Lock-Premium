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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun SessionSummaryScreen(
    focusViewModel: FocusViewModel,
    onDone: () -> Unit
) {
    val lastSession by focusViewModel.lastCompletedSession.collectAsStateWithLifecycle()
    var notes by remember { mutableStateOf("") }

    val isCompleted = lastSession?.completed ?: true
    val elapsedMins = (lastSession?.elapsedSeconds ?: 1500) / 60

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Session Summary",
                onBack = onDone
            )
        },
        bottomBar = {
            Surface(
                color = Charcoal950,
                border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal800)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("summary_done_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text(
                            text = "Return to Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = Charcoal950
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) EmeraldSuccess.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Flag,
                        contentDescription = null,
                        tint = if (isCompleted) EmeraldSuccess else AmberWarning,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isCompleted) "Great Session Completed!" else "Session Concluded",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isCompleted) "You defended your attention and honored your goals." else "Every effort strengthens your focus habit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FOCUSED TIME", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${elapsedMins}m",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PurpleAccent
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MODE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastSession?.mode ?: "Quick",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STATUS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isCompleted) "Success" else "Partial",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) EmeraldSuccess else AmberWarning
                            )
                        }
                    }
                }
            }

            // Reflection Notes Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SESSION REFLECTION (OPTIONAL)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("What did you accomplish during this focus block?", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("session_reflection_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpleAccent,
                                unfocusedBorderColor = Charcoal700,
                                focusedContainerColor = Charcoal900,
                                unfocusedContainerColor = Charcoal900,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}
