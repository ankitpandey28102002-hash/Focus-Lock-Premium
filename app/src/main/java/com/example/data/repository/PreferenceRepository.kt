package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focuslock_prefs")

class PreferenceRepository(private val context: Context) {

    companion object {
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "dark", "light", "system"
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val STRICT_COOLDOWN_SECONDS = intPreferencesKey("strict_cooldown_seconds")
        val STRICT_EMERGENCY_LIMIT = intPreferencesKey("strict_emergency_limit")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DEMO_DATA_LOADED = booleanPreferencesKey("demo_data_loaded")

        // Active session recovery keys
        val ACTIVE_SESSION_RUNNING = booleanPreferencesKey("active_session_running")
        val ACTIVE_SESSION_START_TIME = longPreferencesKey("active_session_start_time")
        val ACTIVE_SESSION_END_TIME = longPreferencesKey("active_session_end_time")
        val ACTIVE_SESSION_DURATION_MINS = intPreferencesKey("active_session_duration_mins")
        val ACTIVE_SESSION_REMAINING_SECS = intPreferencesKey("active_session_remaining_secs")
        val ACTIVE_SESSION_TITLE = stringPreferencesKey("active_session_title")
        val ACTIVE_SESSION_MODE = stringPreferencesKey("active_session_mode")
        val ACTIVE_SESSION_IS_STRICT = booleanPreferencesKey("active_session_is_strict")
        val ACTIVE_SESSION_BLOCK_LIST_ID = longPreferencesKey("active_session_block_list_id")
        val ACTIVE_SESSION_IS_PAUSED = booleanPreferencesKey("active_session_is_paused")
        val ACTIVE_SESSION_PAUSES_COUNT = intPreferencesKey("active_session_pauses_count")
    }

    val dailyGoalMinutes: Flow<Int> = context.dataStore.data.map { it[DAILY_GOAL_MINUTES] ?: 120 }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "dark" }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED] ?: true }
    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { it[VIBRATION_ENABLED] ?: true }
    val strictCooldownSeconds: Flow<Int> = context.dataStore.data.map { it[STRICT_COOLDOWN_SECONDS] ?: 30 }
    val strictEmergencyLimit: Flow<Int> = context.dataStore.data.map { it[STRICT_EMERGENCY_LIMIT] ?: 3 }
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    val isDemoDataLoaded: Flow<Boolean> = context.dataStore.data.map { it[DEMO_DATA_LOADED] ?: false }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[DAILY_GOAL_MINUTES] = minutes }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VIBRATION_ENABLED] = enabled }
    }

    suspend fun setStrictCooldownSeconds(seconds: Int) {
        context.dataStore.edit { it[STRICT_COOLDOWN_SECONDS] = seconds }
    }

    suspend fun setStrictEmergencyLimit(limit: Int) {
        context.dataStore.edit { it[STRICT_EMERGENCY_LIMIT] = limit }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDemoDataLoaded(loaded: Boolean) {
        context.dataStore.edit { it[DEMO_DATA_LOADED] = loaded }
    }

    suspend fun saveActiveSessionState(
        isRunning: Boolean,
        startTime: Long,
        endTime: Long,
        durationMins: Int,
        remainingSecs: Int,
        title: String,
        mode: String,
        isStrict: Boolean,
        blockListId: Long?,
        isPaused: Boolean,
        pausesCount: Int
    ) {
        context.dataStore.edit {
            it[ACTIVE_SESSION_RUNNING] = isRunning
            it[ACTIVE_SESSION_START_TIME] = startTime
            it[ACTIVE_SESSION_END_TIME] = endTime
            it[ACTIVE_SESSION_DURATION_MINS] = durationMins
            it[ACTIVE_SESSION_REMAINING_SECS] = remainingSecs
            it[ACTIVE_SESSION_TITLE] = title
            it[ACTIVE_SESSION_MODE] = mode
            it[ACTIVE_SESSION_IS_STRICT] = isStrict
            it[ACTIVE_SESSION_BLOCK_LIST_ID] = blockListId ?: -1L
            it[ACTIVE_SESSION_IS_PAUSED] = isPaused
            it[ACTIVE_SESSION_PAUSES_COUNT] = pausesCount
        }
    }

    suspend fun clearActiveSessionState() {
        context.dataStore.edit {
            it[ACTIVE_SESSION_RUNNING] = false
            it[ACTIVE_SESSION_START_TIME] = 0L
            it[ACTIVE_SESSION_END_TIME] = 0L
            it[ACTIVE_SESSION_DURATION_MINS] = 0
            it[ACTIVE_SESSION_REMAINING_SECS] = 0
            it[ACTIVE_SESSION_TITLE] = ""
            it[ACTIVE_SESSION_MODE] = ""
            it[ACTIVE_SESSION_IS_STRICT] = false
            it[ACTIVE_SESSION_BLOCK_LIST_ID] = -1L
            it[ACTIVE_SESSION_IS_PAUSED] = false
            it[ACTIVE_SESSION_PAUSES_COUNT] = 0
        }
    }

    val activeSessionState = context.dataStore.data.map { prefs ->
        ActiveSessionSnapshot(
            isRunning = prefs[ACTIVE_SESSION_RUNNING] ?: false,
            startTime = prefs[ACTIVE_SESSION_START_TIME] ?: 0L,
            endTime = prefs[ACTIVE_SESSION_END_TIME] ?: 0L,
            durationMins = prefs[ACTIVE_SESSION_DURATION_MINS] ?: 0,
            remainingSecs = prefs[ACTIVE_SESSION_REMAINING_SECS] ?: 0,
            title = prefs[ACTIVE_SESSION_TITLE] ?: "",
            mode = prefs[ACTIVE_SESSION_MODE] ?: "",
            isStrict = prefs[ACTIVE_SESSION_IS_STRICT] ?: false,
            blockListId = prefs[ACTIVE_SESSION_BLOCK_LIST_ID]?.takeIf { it > 0 },
            isPaused = prefs[ACTIVE_SESSION_IS_PAUSED] ?: false,
            pausesCount = prefs[ACTIVE_SESSION_PAUSES_COUNT] ?: 0
        )
    }
}

data class ActiveSessionSnapshot(
    val isRunning: Boolean,
    val startTime: Long,
    val endTime: Long,
    val durationMins: Int,
    val remainingSecs: Int,
    val title: String,
    val mode: String,
    val isStrict: Boolean,
    val blockListId: Long?,
    val isPaused: Boolean,
    val pausesCount: Int
)
