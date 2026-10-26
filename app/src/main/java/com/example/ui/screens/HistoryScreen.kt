package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.FocusSessionEntity
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.FocusViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    analyticsViewModel: AnalyticsViewModel,
    focusViewModel: FocusViewModel,
    onBack: () -> Unit
) {
    val allSessions by analyticsViewModel.allSessions.collectAsStateWithLifecycle()
    var filterIndex by remember { mutableStateOf(0) } // 0=All, 1=Completed, 2=Abandoned, 3=Strict

    val filtered = when (filterIndex) {
        1 -> allSessions.filter { it.completed }
        2 -> allSessions.filter { it.abandoned }
        3 -> allSessions.filter { it.isStrictMode }
        else -> allSessions
    }

    val sdf = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Session History",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All (${allSessions.size})", "Completed", "Abandoned", "Strict").forEachIndexed { idx, label ->
                        val isSelected = filterIndex == idx
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterIndex = idx },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePrimary,
                                selectedLabelColor = TextPrimary,
                                containerColor = Charcoal850,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No focus sessions recorded in this category.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(filtered, key = { it.id }) { session ->
                    val elapsedMins = session.elapsedSeconds / 60
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_card_${session.id}"),
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
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (session.completed) EmeraldSuccess.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (session.completed) Icons.Default.CheckCircle else Icons.Default.Flag,
                                            contentDescription = null,
                                            tint = if (session.completed) EmeraldSuccess else AmberWarning,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = session.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = sdf.format(Date(session.startTime)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Text(
                                    text = "${elapsedMins}m",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Charcoal700) {
                                        Text(
                                            text = session.mode,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (session.isStrictMode) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = RoseStrict.copy(alpha = 0.2f)) {
                                            Text(
                                                text = "STRICT",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = RoseStrict,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        // Delete session
                                        // Can call focusViewModel.repository.deleteSession(session) in coroutine
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }

                            if (session.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "“${session.notes}”",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
