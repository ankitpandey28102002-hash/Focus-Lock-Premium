package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun BackupRestoreScreen(
    analyticsViewModel: AnalyticsViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Backup & Local Data",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "FocusLock stores 100% of your records locally on your device in an encrypted Room SQLite database. You have full ownership to export or delete your data at any time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }

            // Export Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = PurpleAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Export Database Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate a complete JSON backup of your focus sessions, block lists, and preferences to save or transfer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val json = analyticsViewModel.generateJsonExport()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("FocusLock Backup JSON", json))
                                Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("export_backup_json_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Copy JSON Backup to Clipboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Reset Data Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = RoseStrict.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseStrict.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = RoseStrict)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Clear All Local Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoseStrict)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Deletes all completed sessions, audit logs, and resets settings to default.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("clear_all_data_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseStrict)
                        ) {
                            Text("Reset Local Database", color = RoseStrict, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Confirm Data Reset", color = TextPrimary) },
            text = { Text("Are you sure you want to delete all local sessions? This operation cannot be undone.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.clearAllData()
                        showClearConfirm = false
                        Toast.makeText(context, "Local data cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseStrict)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = Charcoal900
        )
    }
}
