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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusMode
import com.example.ui.components.FocusLockTopBar
import com.example.ui.components.UnlockedProBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun FocusModeSelectorScreen(
    focusViewModel: FocusViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val modes = FocusMode.values()

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Focus Modes",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Every focus discipline is unlocked without limits. Choose the mode matching your current goal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(modes) { mode ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            focusViewModel.selectMode(mode)
                            onNavigate(Screen.TimerSetup.route)
                        }
                        .testTag("focus_mode_card_${mode.name}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (mode.isStrictByDefault) RoseStrict.copy(alpha = 0.2f) else PurplePrimary.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (mode) {
                                        FocusMode.QUICK -> Icons.Default.FlashOn
                                        FocusMode.POMODORO -> Icons.Default.Timer
                                        FocusMode.DEEP_FOCUS -> Icons.Default.Psychology
                                        FocusMode.STRICT -> Icons.Default.Lock
                                        FocusMode.STUDY -> Icons.Default.School
                                        FocusMode.WORK -> Icons.Default.Work
                                        FocusMode.SLEEP -> Icons.Default.Bedtime
                                        FocusMode.CUSTOM -> Icons.Default.Tune
                                    },
                                    contentDescription = null,
                                    tint = if (mode.isStrictByDefault) RoseStrict else PurpleAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (mode.isStrictByDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = RoseStrict.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "STRICT",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = RoseStrict,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = mode.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Charcoal700
                        ) {
                            Text(
                                text = "${mode.defaultDurationMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PurpleAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
