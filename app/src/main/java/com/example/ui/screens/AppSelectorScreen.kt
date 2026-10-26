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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InstalledApp
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.BlockListViewModel

@Composable
fun AppSelectorScreen(
    blockListViewModel: BlockListViewModel,
    onDone: () -> Unit
) {
    val searchQuery by blockListViewModel.searchQuery.collectAsStateWithLifecycle()
    val showSystemApps by blockListViewModel.showSystemApps.collectAsStateWithLifecycle()
    val filteredApps by blockListViewModel.filteredApps.collectAsStateWithLifecycle()
    val selectedPackages by blockListViewModel.selectedPackages.collectAsStateWithLifecycle()
    val isLoading by blockListViewModel.isLoadingApps.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Select Apps to Block",
                onBack = onDone,
                actions = {
                    TextButton(
                        onClick = onDone,
                        modifier = Modifier.testTag("app_selector_done_btn")
                    ) {
                        Text("Done (${selectedPackages.size})", color = PurpleAccent, fontWeight = FontWeight.Bold)
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
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { blockListViewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("app_search_input"),
                placeholder = { Text("Search installed applications...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { blockListViewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleAccent,
                    unfocusedBorderColor = Charcoal700,
                    focusedContainerColor = Charcoal900,
                    unfocusedContainerColor = Charcoal900,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            // Selection Controls & System Apps Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { blockListViewModel.selectAllFiltered() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Select All", color = PurpleAccent, style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(
                        onClick = { blockListViewModel.deselectAll() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Clear All", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Show System", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = showSystemApps,
                        onCheckedChange = { blockListViewModel.toggleSystemApps(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PurpleAccent,
                            checkedTrackColor = PurpleDark,
                            uncheckedTrackColor = Charcoal700
                        )
                    )
                }
            }

            // Apps List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PurpleAccent)
                }
            } else if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No apps found matching '$searchQuery'",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { appItem ->
                        AppItemRow(
                            app = appItem,
                            isSelected = selectedPackages.contains(appItem.packageName),
                            onToggle = { blockListViewModel.toggleAppSelection(appItem.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItemRow(
    app: InstalledApp,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("app_item_${app.packageName}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Charcoal800 else Charcoal850
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PurpleAccent.copy(alpha = 0.6f) else Charcoal700
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                            when (app.category) {
                                "Social" -> Color(0xFFE1306C).copy(alpha = 0.2f)
                                "Entertainment" -> Color(0xFFFF0000).copy(alpha = 0.2f)
                                "Games" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                else -> PurplePrimary.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (app.category) {
                            "Social" -> Icons.Default.Share
                            "Entertainment" -> Icons.Default.PlayCircle
                            "Games" -> Icons.Default.SportsEsports
                            "Browsers" -> Icons.Default.Language
                            else -> Icons.Default.Apps
                        },
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Charcoal700
                        ) {
                            Text(
                                text = app.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        if (app.isSystemApp) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• System",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PurpleAccent,
                    checkmarkColor = Charcoal950,
                    uncheckedColor = Charcoal600
                )
            )
        }
    }
}
