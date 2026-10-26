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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun TimerSetupScreen(
    focusViewModel: FocusViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val selectedMode by focusViewModel.selectedMode.collectAsStateWithLifecycle()
    val durationMinutes by focusViewModel.selectedDurationMinutes.collectAsStateWithLifecycle()
    val sessionTitle by focusViewModel.sessionTitle.collectAsStateWithLifecycle()
    val isStrictMode by focusViewModel.isStrictMode.collectAsStateWithLifecycle()
    val selectedBlockListId by focusViewModel.selectedBlockListId.collectAsStateWithLifecycle()
    val blockLists by focusViewModel.allBlockLists.collectAsStateWithLifecycle()

    val presetDurations = listOf(5, 10, 15, 25, 30, 45, 60, 90, 120)

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Start Focus Session",
                onBack = onBack
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
                        onClick = {
                            focusViewModel.startSession()
                            onNavigate(Screen.ActiveSession.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("launch_focus_session_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStrictMode) RoseStrict else PurplePrimary
                        )
                    ) {
                        Icon(
                            if (isStrictMode) Icons.Default.Lock else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isStrictMode) "Activate Strict Lock ($durationMinutes min)" else "Start Focus ($durationMinutes min)",
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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SESSION TITLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = sessionTitle,
                            onValueChange = { focusViewModel.setTitle(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("session_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
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

            // Duration Selector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SESSION DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "$durationMinutes",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent
                        )
                        Text(
                            text = "MINUTES",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Slider(
                            value = durationMinutes.toFloat(),
                            onValueChange = { focusViewModel.setDuration(it.toInt()) },
                            valueRange = 5f..180f,
                            steps = 34,
                            colors = SliderDefaults.colors(
                                thumbColor = PurpleAccent,
                                activeTrackColor = PurplePrimary,
                                inactiveTrackColor = Charcoal700
                            ),
                            modifier = Modifier.testTag("duration_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick duration preset chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presetDurations) { mins ->
                                val isSelected = mins == durationMinutes
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { focusViewModel.setDuration(mins) },
                                    label = { Text("${mins}m") },
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

            // Block List Picker Card
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
                                text = "ACTIVE BLOCK LIST",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage Lists",
                                style = MaterialTheme.typography.labelSmall,
                                color = PurpleAccent,
                                modifier = Modifier.clickable { onNavigate(Screen.BlockListManager.route) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (blockLists.isEmpty()) {
                            Text(
                                text = "No block list selected. Tap below to create one.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(blockLists) { bl ->
                                    val isSelected = bl.id == selectedBlockListId
                                    Card(
                                        modifier = Modifier
                                            .clickable { focusViewModel.setBlockListId(bl.id) }
                                            .testTag("select_block_list_${bl.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Charcoal700 else Charcoal900
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) PurpleAccent else Charcoal600
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = if (isSelected) PurpleAccent else TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${bl.name} (${bl.getPackageList().size})",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSelected) TextPrimary else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Strict Mode Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStrictMode) RoseStrict.copy(alpha = 0.1f) else Charcoal850
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isStrictMode) RoseStrict.copy(alpha = 0.5f) else Charcoal700
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isStrictMode) RoseStrict.copy(alpha = 0.2f) else Charcoal700
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isStrictMode) RoseStrict else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Strict Mode",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isStrictMode) RoseStrict else TextPrimary
                                    )
                                    Text(
                                        text = "Disables easy pause/exit controls",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isStrictMode,
                                onCheckedChange = { focusViewModel.setStrictMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RoseStrict,
                                    checkedTrackColor = RoseStrict.copy(alpha = 0.3f),
                                    uncheckedTrackColor = Charcoal700
                                ),
                                modifier = Modifier.testTag("strict_mode_switch")
                            )
                        }

                        if (isStrictMode) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Strict Mode resists casual abandonment. Emergency phone calls and system accessibility remain fully operational.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
