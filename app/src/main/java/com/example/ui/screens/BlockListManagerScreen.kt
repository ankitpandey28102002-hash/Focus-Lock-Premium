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
import com.example.data.model.BlockListEntity
import com.example.ui.components.FocusLockTopBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.BlockListViewModel

@Composable
fun BlockListManagerScreen(
    blockListViewModel: BlockListViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val blockLists by blockListViewModel.allBlockLists.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "App Block Lists",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    blockListViewModel.startCreateNew()
                    showCreateDialog = true
                },
                containerColor = PurplePrimary,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("create_block_list_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Block List")
            }
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
                    text = "Configure custom lists to isolate distracting apps during work, study, gaming limits, or bedtime.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(items = blockLists, key = { it.id }) { list ->
                val packageCount = list.getPackageList().size
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            blockListViewModel.startEdit(list)
                            showCreateDialog = true
                        }
                        .testTag("block_list_card_${list.id}"),
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
                                        .background(PurplePrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (list.icon) {
                                            "Share" -> Icons.Default.Share
                                            "Work" -> Icons.Default.Work
                                            "SportsEsports" -> Icons.Default.SportsEsports
                                            "Bedtime" -> Icons.Default.Bedtime
                                            else -> Icons.Default.Shield
                                        },
                                        contentDescription = null,
                                        tint = PurpleAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = list.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$packageCount apps locked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (packageCount > 0) EmeraldSuccess else TextMuted
                                    )
                                }
                            }

                            Row {
                                if (list.isSystemDefault) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Charcoal700
                                    ) {
                                        Text(
                                            text = "PRESET",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { blockListViewModel.deleteBlockList(list) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = RoseStrict,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (list.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = list.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                blockListViewModel.startEdit(list)
                                onNavigate(Screen.AppSelector.route)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("edit_apps_btn_${list.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal700)
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Selected Apps ($packageCount)", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        val editingName by blockListViewModel.listName.collectAsStateWithLifecycle()
        val editingDesc by blockListViewModel.listDescription.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = if (blockListViewModel.editingBlockListId.value == null) "Create Block List" else "Edit Block List",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { blockListViewModel.setListName(it) },
                        label = { Text("List Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("block_list_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editingDesc,
                        onValueChange = { blockListViewModel.setListDescription(it) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        blockListViewModel.saveBlockList {
                            showCreateDialog = false
                            onNavigate(Screen.AppSelector.route)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    modifier = Modifier.testTag("save_and_pick_apps_btn")
                ) {
                    Text("Select Apps")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Charcoal900
        )
    }
}
