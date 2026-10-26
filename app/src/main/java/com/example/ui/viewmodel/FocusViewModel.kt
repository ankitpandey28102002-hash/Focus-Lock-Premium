package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FocusLockApplication
import com.example.data.model.*
import com.example.service.FocusSessionService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApplication
    val repository = app.repository
    val preferenceRepository = app.preferenceRepository

    val serviceSessionState = FocusSessionService.sessionState

    val todayFocusedMinutes: StateFlow<Int> = repository.todayFocusedMinutes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val dailyGoalMinutes: StateFlow<Int> = preferenceRepository.dailyGoalMinutes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 120
    )

    val streakDays: StateFlow<Int> = repository.focusStreakDays.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val allBlockLists: StateFlow<List<BlockListEntity>> = repository.allBlockLists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allProfiles: StateFlow<List<FocusProfileEntity>> = repository.allProfiles.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allSchedules: StateFlow<List<ScheduleEntity>> = repository.allSchedules.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val completedSessions: StateFlow<List<FocusSessionEntity>> = repository.completedSessions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Current Timer Setup draft state
    private val _selectedMode = MutableStateFlow(FocusMode.QUICK)
    val selectedMode = _selectedMode.asStateFlow()

    private val _selectedDurationMinutes = MutableStateFlow(25)
    val selectedDurationMinutes = _selectedDurationMinutes.asStateFlow()

    private val _sessionTitle = MutableStateFlow("Quick Focus Sprint")
    val sessionTitle = _sessionTitle.asStateFlow()

    private val _isStrictMode = MutableStateFlow(false)
    val isStrictMode = _isStrictMode.asStateFlow()

    private val _selectedBlockListId = MutableStateFlow<Long?>(null)
    val selectedBlockListId = _selectedBlockListId.asStateFlow()

    // Last completed session for summary screen
    private val _lastCompletedSession = MutableStateFlow<FocusSessionEntity?>(null)
    val lastCompletedSession = _lastCompletedSession.asStateFlow()

    init {
        // Automatically default to first block list if available
        viewModelScope.launch {
            allBlockLists.collect { lists ->
                if (_selectedBlockListId.value == null && lists.isNotEmpty()) {
                    _selectedBlockListId.value = lists.first().id
                }
            }
        }
    }

    fun selectMode(mode: FocusMode) {
        _selectedMode.value = mode
        _selectedDurationMinutes.value = mode.defaultDurationMinutes
        _sessionTitle.value = mode.title
        _isStrictMode.value = mode.isStrictByDefault
    }

    fun applyProfile(profile: FocusProfileEntity) {
        val mode = try { FocusMode.valueOf(profile.mode) } catch (e: Exception) { FocusMode.CUSTOM }
        _selectedMode.value = mode
        _selectedDurationMinutes.value = profile.defaultDurationMinutes
        _sessionTitle.value = profile.name
        _isStrictMode.value = profile.isStrict
        if (profile.blockListId != null) {
            _selectedBlockListId.value = profile.blockListId
        }
    }

    fun setDuration(minutes: Int) {
        _selectedDurationMinutes.value = minutes.coerceIn(1, 480)
    }

    fun setTitle(title: String) {
        _sessionTitle.value = title
    }

    fun setStrictMode(enabled: Boolean) {
        _isStrictMode.value = enabled
    }

    fun setBlockListId(id: Long?) {
        _selectedBlockListId.value = id
    }

    fun startSession() {
        val intent = Intent(app, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
            putExtra(FocusSessionService.EXTRA_TITLE, _sessionTitle.value.ifBlank { "Focus Sprint" })
            putExtra(FocusSessionService.EXTRA_MODE, _selectedMode.value.name)
            putExtra(FocusSessionService.EXTRA_DURATION_MINS, _selectedDurationMinutes.value)
            putExtra(FocusSessionService.EXTRA_IS_STRICT, _isStrictMode.value)
            putExtra(FocusSessionService.EXTRA_BLOCK_LIST_ID, _selectedBlockListId.value ?: -1L)
        }
        app.startService(intent)
    }

    fun pauseSession() {
        val intent = Intent(app, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_PAUSE
        }
        app.startService(intent)
    }

    fun resumeSession() {
        val intent = Intent(app, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_RESUME
        }
        app.startService(intent)
    }

    fun finishSession(abandoned: Boolean = false) {
        val current = serviceSessionState.value
        _lastCompletedSession.value = FocusSessionEntity(
            title = current.title,
            mode = current.mode.name,
            durationMinutes = current.durationMinutes,
            elapsedSeconds = current.elapsedSeconds,
            completed = !abandoned,
            abandoned = abandoned,
            startTime = current.startTime,
            endTime = System.currentTimeMillis(),
            blockListId = current.blockListId,
            pausesCount = current.pausesCount,
            isStrictMode = current.isStrict
        )

        val intent = Intent(app, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_FINISH
        }
        app.startService(intent)
    }
}
