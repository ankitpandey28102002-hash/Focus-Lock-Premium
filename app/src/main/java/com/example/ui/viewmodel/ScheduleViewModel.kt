package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FocusLockApplication
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApplication
    private val repository = app.repository

    val allSchedules: StateFlow<List<ScheduleEntity>> = repository.allSchedules.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allBlockLists: StateFlow<List<BlockListEntity>> = repository.allBlockLists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Schedule Editor state
    private val _editingScheduleId = MutableStateFlow<Long?>(null)
    val editingScheduleId = _editingScheduleId.asStateFlow()

    private val _scheduleName = MutableStateFlow("")
    val scheduleName = _scheduleName.asStateFlow()

    private val _startHour = MutableStateFlow(9)
    val startHour = _startHour.asStateFlow()

    private val _startMinute = MutableStateFlow(0)
    val startMinute = _startMinute.asStateFlow()

    private val _endHour = MutableStateFlow(17)
    val endHour = _endHour.asStateFlow()

    private val _endMinute = MutableStateFlow(0)
    val endMinute = _endMinute.asStateFlow()

    private val _selectedDays = MutableStateFlow(setOf(1, 2, 3, 4, 5)) // Mon-Fri
    val selectedDays = _selectedDays.asStateFlow()

    private val _selectedBlockListId = MutableStateFlow<Long?>(null)
    val selectedBlockListId = _selectedBlockListId.asStateFlow()

    private val _isStrict = MutableStateFlow(false)
    val isStrict = _isStrict.asStateFlow()

    fun startCreateNew() {
        _editingScheduleId.value = null
        _scheduleName.value = "Productive Hours"
        _startHour.value = 9
        _startMinute.value = 0
        _endHour.value = 17
        _endMinute.value = 0
        _selectedDays.value = setOf(1, 2, 3, 4, 5)
        _isStrict.value = false
        _selectedBlockListId.value = allBlockLists.value.firstOrNull()?.id
    }

    fun startEdit(schedule: ScheduleEntity) {
        _editingScheduleId.value = schedule.id
        _scheduleName.value = schedule.name
        _startHour.value = schedule.startHour
        _startMinute.value = schedule.startMinute
        _endHour.value = schedule.endHour
        _endMinute.value = schedule.endMinute
        _selectedDays.value = schedule.getDaysList().toSet()
        _isStrict.value = schedule.isStrict
        _selectedBlockListId.value = schedule.blockListId
    }

    fun setScheduleName(name: String) { _scheduleName.value = name }
    fun setStartTime(hour: Int, min: Int) { _startHour.value = hour; _startMinute.value = min }
    fun setEndTime(hour: Int, min: Int) { _endHour.value = hour; _endMinute.value = min }
    fun toggleDay(day: Int) {
        val current = _selectedDays.value.toMutableSet()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _selectedDays.value = current
    }
    fun setBlockListId(id: Long?) { _selectedBlockListId.value = id }
    fun setStrict(strict: Boolean) { _isStrict.value = strict }

    fun toggleScheduleEnabled(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.updateSchedule(schedule.copy(isEnabled = !schedule.isEnabled))
        }
    }

    fun saveSchedule(onSaved: () -> Unit) {
        viewModelScope.launch {
            val daysStr = _selectedDays.value.sorted().joinToString(",")
            val id = _editingScheduleId.value
            if (id == null) {
                repository.insertSchedule(
                    ScheduleEntity(
                        name = _scheduleName.value.ifBlank { "Focus Routine" },
                        startHour = _startHour.value,
                        startMinute = _startMinute.value,
                        endHour = _endHour.value,
                        endMinute = _endMinute.value,
                        daysOfWeek = daysStr,
                        blockListId = _selectedBlockListId.value,
                        isEnabled = true,
                        isStrict = _isStrict.value
                    )
                )
            } else {
                repository.updateSchedule(
                    ScheduleEntity(
                        id = id,
                        name = _scheduleName.value.ifBlank { "Focus Routine" },
                        startHour = _startHour.value,
                        startMinute = _startMinute.value,
                        endHour = _endHour.value,
                        endMinute = _endMinute.value,
                        daysOfWeek = daysStr,
                        blockListId = _selectedBlockListId.value,
                        isEnabled = true,
                        isStrict = _isStrict.value
                    )
                )
            }
            onSaved()
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    // Schedule conflict detection
    fun checkConflicts(candidate: ScheduleEntity): List<ScheduleEntity> {
        val candidateDays = candidate.getDaysList().toSet()
        val candidateStart = candidate.startHour * 60 + candidate.startMinute
        val candidateEnd = candidate.endHour * 60 + candidate.endMinute

        return allSchedules.value.filter { existing ->
            existing.id != candidate.id && existing.isEnabled &&
                    existing.getDaysList().any { candidateDays.contains(it) } &&
                    run {
                        val exStart = existing.startHour * 60 + existing.startMinute
                        val exEnd = existing.endHour * 60 + existing.endMinute
                        candidateStart < exEnd && candidateEnd > exStart
                    }
        }
    }
}
