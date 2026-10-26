package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.FocusLockTopBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScheduleViewModel

@Composable
fun ScheduleManagerScreen(
    scheduleViewModel: ScheduleViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val schedules by scheduleViewModel.allSchedules.collectAsStateWithLifecycle()
    val blockLists by scheduleViewModel.allBlockLists.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Focus Schedules",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { onNavigate(Screen.WeeklyCalendar.route) },
                        modifier = Modifier.testTag("open_weekly_calendar_btn")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Weekly Calendar", tint = PurpleAccent)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scheduleViewModel.startCreateNew()
                    showEditDialog = true
                },
                containerColor = PurplePrimary,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("create_schedule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
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
                    text = "Automate your daily focus habits. Scheduled routines automatically enforce your block lists at predetermined hours.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            items(items = schedules, key = { it.id }) { schedule ->
                val assignedBlockList = blockLists.firstOrNull { it.id == schedule.blockListId }
                val startFormatted = String.format("%02d:%02d", schedule.startHour, schedule.startMinute)
                val endFormatted = String.format("%02d:%02d", schedule.endHour, schedule.endMinute)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scheduleViewModel.startEdit(schedule)
                            showEditDialog = true
                        }
                        .testTag("schedule_card_${schedule.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(IndigoAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = IndigoAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = schedule.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$startFormatted - $endFormatted",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PurpleAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Switch(
                                checked = schedule.isEnabled,
                                onCheckedChange = { scheduleViewModel.toggleScheduleEnabled(schedule) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PurpleAccent,
                                    checkedTrackColor = PurpleDark,
                                    uncheckedTrackColor = Charcoal700
                                ),
                                modifier = Modifier.testTag("toggle_schedule_${schedule.id}")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Days of week pills
                        val daysMap = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
                        val activeDays = schedule.getDaysList().toSet()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            daysMap.forEach { (dayInt, label) ->
                                val isActive = activeDays.contains(dayInt)
                                Surface(
                                    shape = CircleShape,
                                    color = if (isActive) PurplePrimary else Charcoal800,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isActive) TextPrimary else TextMuted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Block List: ${assignedBlockList?.name ?: "All Distractions"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )

                            IconButton(
                                onClick = { scheduleViewModel.deleteSchedule(schedule) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        ScheduleEditDialog(
            scheduleViewModel = scheduleViewModel,
            blockLists = blockLists,
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
private fun ScheduleEditDialog(
    scheduleViewModel: ScheduleViewModel,
    blockLists: List<com.example.data.model.BlockListEntity>,
    onDismiss: () -> Unit
) {
    val name by scheduleViewModel.scheduleName.collectAsStateWithLifecycle()
    val startHour by scheduleViewModel.startHour.collectAsStateWithLifecycle()
    val startMin by scheduleViewModel.startMinute.collectAsStateWithLifecycle()
    val endHour by scheduleViewModel.endHour.collectAsStateWithLifecycle()
    val endMin by scheduleViewModel.endMinute.collectAsStateWithLifecycle()
    val selectedDays by scheduleViewModel.selectedDays.collectAsStateWithLifecycle()
    val isStrict by scheduleViewModel.isStrict.collectAsStateWithLifecycle()
    val selectedBlockListId by scheduleViewModel.selectedBlockListId.collectAsStateWithLifecycle()

    val daysMap = listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Focus Routine", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { scheduleViewModel.setScheduleName(it) },
                    label = { Text("Routine Name") },
                    modifier = Modifier.fillMaxWidth().testTag("schedule_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("Active Days", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    daysMap.forEach { (dayInt, label) ->
                        val isSelected = selectedDays.contains(dayInt)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PurplePrimary else Charcoal800,
                            modifier = Modifier
                                .clickable { scheduleViewModel.toggleDay(dayInt) }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Strict Mode Protection", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Switch(
                        checked = isStrict,
                        onCheckedChange = { scheduleViewModel.setStrict(it) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scheduleViewModel.saveSchedule { onDismiss() }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                modifier = Modifier.testTag("save_schedule_btn")
            ) {
                Text("Save Routine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = Charcoal900
    )
}
