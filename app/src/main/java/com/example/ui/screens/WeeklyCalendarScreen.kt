package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScheduleEntity
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScheduleViewModel
import java.util.Calendar

@Composable
fun WeeklyCalendarScreen(
    scheduleViewModel: ScheduleViewModel,
    onBack: () -> Unit
) {
    val schedules by scheduleViewModel.allSchedules.collectAsStateWithLifecycle()
    val enabledSchedules = schedules.filter { it.isEnabled }

    val days = listOf(
        1 to "Monday",
        2 to "Tuesday",
        3 to "Wednesday",
        4 to "Thursday",
        5 to "Friday",
        6 to "Saturday",
        7 to "Sunday"
    )

    val currentDayOfWeek = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Weekly Calendar",
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
                    text = "Weekly distribution of your automated focus routines and app blocking rules.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            items(days) { (dayInt, dayName) ->
                val daySchedules = enabledSchedules.filter { it.getDaysList().contains(dayInt) }
                val isToday = dayInt == currentDayOfWeek

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isToday) Charcoal800 else Charcoal850
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isToday) PurpleAccent else Charcoal700
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
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PurplePrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurpleAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (daySchedules.isEmpty()) "Free Day" else "${daySchedules.size} Focus Blocks",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (daySchedules.isEmpty()) TextMuted else EmeraldGlow
                            )
                        }

                        if (daySchedules.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No scheduled routines for this day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            daySchedules.forEach { s ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Charcoal900,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = IndigoAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = s.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        val startFmt = String.format("%02d:%02d", s.startHour, s.startMinute)
                                        val endFmt = String.format("%02d:%02d", s.endHour, s.endMinute)
                                        Text(
                                            text = "$startFmt - $endFmt",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PurpleAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
