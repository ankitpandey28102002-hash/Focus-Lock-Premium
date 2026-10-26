package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.FocusLockAppNavigation
import com.example.ui.theme.Charcoal950
import com.example.ui.theme.FocusLockTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val focusViewModel = ViewModelProvider(this)[FocusViewModel::class.java]
        val blockListViewModel = ViewModelProvider(this)[BlockListViewModel::class.java]
        val scheduleViewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]
        val analyticsViewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        val settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            val isDark = when (themeMode) {
                "light" -> false
                "dark", "violet" -> true
                else -> systemDark
            }

            FocusLockTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Charcoal950
                ) {
                    FocusLockAppNavigation(
                        focusViewModel = focusViewModel,
                        blockListViewModel = blockListViewModel,
                        scheduleViewModel = scheduleViewModel,
                        analyticsViewModel = analyticsViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
