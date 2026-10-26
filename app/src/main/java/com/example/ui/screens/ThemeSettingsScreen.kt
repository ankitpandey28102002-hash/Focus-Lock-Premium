package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FocusLockTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun ThemeSettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val currentTheme by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    val themes = listOf(
        ThemeOption("dark", "Deep Charcoal Pro", "Dark minimalist palette engineered for deep work", Charcoal950, PurpleAccent),
        ThemeOption("violet", "Midnight Violet", "Rich obsidian and glowing indigo hues", Color(0xFF0F0B1E), Color(0xFFA78BFA)),
        ThemeOption("light", "Clean Minimalist Light", "High-contrast day theme for bright environments", Color(0xFFF8FAFC), Color(0xFF6D28D9)),
        ThemeOption("system", "System Default", "Follows device dark/light OS appearance", Charcoal900, TextSecondary)
    )

    Scaffold(
        topBar = {
            FocusLockTopBar(
                title = "Theme Customization",
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
                    text = "Personalize the visual identity of FocusLock. All dark and light themes are fully unlocked.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            items(themes.size) { idx ->
                val theme = themes[idx]
                val isSelected = currentTheme == theme.key

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { settingsViewModel.setThemeMode(theme.key) }
                        .testTag("theme_card_${theme.key}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal850),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) PurpleAccent else Charcoal700
                    )
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(theme.previewBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(theme.accent)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = theme.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = theme.desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = PurpleAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ThemeOption(
    val key: String,
    val title: String,
    val desc: String,
    val previewBg: Color,
    val accent: Color
)
