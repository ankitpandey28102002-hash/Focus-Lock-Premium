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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.components.UnlockedProBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun StrictModeConfigScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val cooldownSeconds by settingsViewModel.strictCooldownSeconds.collectAsStateWithLifecycle()
    val emergencyLimit by settingsViewModel.strictEmergencyLimit.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Strict Mode Defenses",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = RoseStrict.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseStrict.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(RoseStrict.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = RoseStrict,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Unbreakable Personal Discipline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Strict mode removes the temptation to quickly pause or unlock during a sprint.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Realistic Android Enforcement Transparency Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TECHNICAL ENFORCEMENT & SAFETY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• Enforced: Immediate redirection back to home screen when blocked apps are detected.\n" +
                                    "• Enforced: Pause button disabled during strict sessions.\n" +
                                    "• Enforced: Mandatory countdown cooldown before emergency exit is allowed.\n" +
                                    "• Preserved for Safety: Emergency phone dialer (911/112), incoming urgent calls, and system device settings always remain accessible.\n" +
                                    "• Realistic note: As a non-root user app, Android OS does not permit standard applications to block OS uninstallation or force-stop.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Cooldown Period Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EMERGENCY EXIT COOLDOWN",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Forces you to wait before an emergency exit can be confirmed, giving your brain time to reconsider.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(10, 30, 60).forEach { seconds ->
                                val isSelected = cooldownSeconds == seconds
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { settingsViewModel.setStrictCooldown(seconds) },
                                    label = { Text("${seconds}s Cooldown") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoseStrict,
                                        selectedLabelColor = TextPrimary,
                                        containerColor = Charcoal900,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("cooldown_${seconds}s_chip")
                                )
                            }
                        }
                    }
                }
            }

            // Emergency Allowance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MAX EMERGENCY OVERRIDES PER DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cap the number of times you can trigger emergency mode to prevent abusing the override.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(1, 3, 5).forEach { limit ->
                                val isSelected = emergencyLimit == limit
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { settingsViewModel.setStrictEmergencyLimit(limit) },
                                    label = { Text("$limit per Day") },
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
        }
    }
}
