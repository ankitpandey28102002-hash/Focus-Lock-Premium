package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun NotificationSettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val soundEnabled by settingsViewModel.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by settingsViewModel.vibrationEnabled.collectAsStateWithLifecycle()

    var preSessionAlert by remember { mutableStateOf(true) }
    var completionFanfare by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Notifications & Audio",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Configure subtle auditory signals and haptic feedback to keep you anchored without breaking concentration.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        NotificationToggleRow(
                            title = "Sound Chimes",
                            subtitle = "Gentle tone upon starting and completing sessions",
                            checked = soundEnabled,
                            onCheckedChange = { settingsViewModel.setSoundEnabled(it) },
                            testTag = "sound_toggle"
                        )

                        HorizontalDivider(color = Charcoal700)

                        NotificationToggleRow(
                            title = "Vibration Haptics",
                            subtitle = "Tactile vibration confirmation on timer ticks and completion",
                            checked = vibrationEnabled,
                            onCheckedChange = { settingsViewModel.setVibrationEnabled(it) },
                            testTag = "vibration_toggle"
                        )

                        HorizontalDivider(color = Charcoal700)

                        NotificationToggleRow(
                            title = "Pre-Schedule Reminders",
                            subtitle = "Alert you 5 minutes before scheduled focus routines begin",
                            checked = preSessionAlert,
                            onCheckedChange = { preSessionAlert = it },
                            testTag = "pre_schedule_toggle"
                        )

                        HorizontalDivider(color = Charcoal700)

                        NotificationToggleRow(
                            title = "Completion Fanfare",
                            subtitle = "Congratulatory notification and streak update",
                            checked = completionFanfare,
                            onCheckedChange = { completionFanfare = it },
                            testTag = "completion_toggle"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PurpleAccent,
                checkedTrackColor = PurpleDark,
                uncheckedTrackColor = Charcoal700
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
