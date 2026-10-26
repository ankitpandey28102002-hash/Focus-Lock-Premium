package com.example.ui.viewmodel

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.FocusLockApplication
import com.example.service.FocusAccessibilityService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApplication
    private val repository = app.repository
    private val pref = app.preferenceRepository

    val dailyGoalMinutes = pref.dailyGoalMinutes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)
    val themeMode = pref.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "dark")
    val soundEnabled = pref.soundEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val vibrationEnabled = pref.vibrationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val strictCooldownSeconds = pref.strictCooldownSeconds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)
    val strictEmergencyLimit = pref.strictEmergencyLimit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)
    val isDemoDataLoaded = pref.isDemoDataLoaded.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isOnboardingCompleted = pref.isOnboardingCompleted.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @Composable
    fun appContextIsOnboardingDone(): State<Boolean> = pref.isOnboardingCompleted.collectAsStateWithLifecycle(initialValue = false)

    fun completeOnboarding() {
        viewModelScope.launch { pref.setOnboardingCompleted(true) }
    }

    // Permission states
    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled = _isAccessibilityEnabled.asStateFlow()

    private val _isUsageStatsGranted = MutableStateFlow(false)
    val isUsageStatsGranted = _isUsageStatsGranted.asStateFlow()

    private val _isOverlayGranted = MutableStateFlow(false)
    val isOverlayGranted = _isOverlayGranted.asStateFlow()

    private val _isNotificationsGranted = MutableStateFlow(false)
    val isNotificationsGranted = _isNotificationsGranted.asStateFlow()

    init {
        refreshPermissionStates()
    }

    fun refreshPermissionStates() {
        _isAccessibilityEnabled.value = checkAccessibilityPermission()
        _isUsageStatsGranted.value = checkUsageStatsPermission()
        _isOverlayGranted.value = checkOverlayPermission()
        _isNotificationsGranted.value = checkNotificationPermission()
    }

    private fun checkAccessibilityPermission(): Boolean {
        val expectedServiceName = "${app.packageName}/${FocusAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            app.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedServiceName) || enabledServices.contains(app.packageName)
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = app.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                app.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                app.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(app)
        } else {
            true
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return NotificationManagerCompat.from(app).areNotificationsEnabled()
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openUsageStatsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${app.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, app.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${app.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        context.startActivity(intent)
    }

    fun setDailyGoal(minutes: Int) {
        viewModelScope.launch { pref.setDailyGoalMinutes(minutes) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { pref.setThemeMode(mode) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { pref.setSoundEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { pref.setVibrationEnabled(enabled) }
    }

    fun setStrictCooldown(seconds: Int) {
        viewModelScope.launch { pref.setStrictCooldownSeconds(seconds) }
    }

    fun setStrictEmergencyLimit(limit: Int) {
        viewModelScope.launch { pref.setStrictEmergencyLimit(limit) }
    }

    fun seedDemoData() {
        viewModelScope.launch { repository.seedDemoData() }
    }

    fun clearAllData() {
        viewModelScope.launch { repository.clearAllData() }
    }
}
