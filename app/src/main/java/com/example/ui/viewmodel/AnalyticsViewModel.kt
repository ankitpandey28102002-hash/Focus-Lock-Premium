package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FocusLockApplication
import com.example.data.model.BlockAuditLogEntity
import com.example.data.model.FocusSessionEntity
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApplication
    private val repository = app.repository

    val allSessions: StateFlow<List<FocusSessionEntity>> = repository.allSessions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val recentAuditLogs: StateFlow<List<BlockAuditLogEntity>> = repository.recentAuditLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val streakDays: StateFlow<Int> = repository.focusStreakDays.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    // Range selector: 0 = 7 Days, 1 = 30 Days, 2 = All Time
    private val _selectedRangeIndex = MutableStateFlow(0)
    val selectedRangeIndex: StateFlow<Int> = _selectedRangeIndex.asStateFlow()

    fun setSelectedRange(index: Int) {
        _selectedRangeIndex.value = index
    }

    // Filtered sessions based on date range
    val filteredSessions: StateFlow<List<FocusSessionEntity>> = combine(allSessions, _selectedRangeIndex) { sessions, rangeIdx ->
        val cutoff = when (rangeIdx) {
            0 -> System.currentTimeMillis() - (7L * 24 * 3600 * 1000)
            1 -> System.currentTimeMillis() - (30L * 24 * 3600 * 1000)
            else -> 0L
        }
        sessions.filter { it.startTime >= cutoff }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary statistics
    val totalFocusedMinutes: StateFlow<Int> = filteredSessions.map { sessions ->
        sessions.sumOf { it.elapsedSeconds } / 60
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedCount: StateFlow<Int> = filteredSessions.map { sessions ->
        sessions.count { it.completed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val abandonedCount: StateFlow<Int> = filteredSessions.map { sessions ->
        sessions.count { it.abandoned }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completionRate: StateFlow<Int> = combine(completedCount, abandonedCount) { comp, aban ->
        val total = comp + aban
        if (total == 0) 100 else ((comp.toFloat() / total) * 100).toInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    // Daily breakdown for the last 7 days (Day name + focused minutes)
    val last7DaysData: StateFlow<List<DayFocusData>> = allSessions.map { sessions ->
        val result = mutableListOf<DayFocusData>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis - (i * 24L * 3600 * 1000)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMs = dayCal.timeInMillis
            val endMs = startMs + (24L * 3600 * 1000) - 1

            val mins = sessions
                .filter { it.startTime in startMs..endMs }
                .sumOf { it.elapsedSeconds } / 60

            val label = dayFormat.format(dayCal.time)
            result.add(DayFocusData(label, mins))
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Most frequently blocked apps
    val topBlockedApps: StateFlow<List<AppBlockStat>> = recentAuditLogs.map { logs ->
        logs.groupBy { it.appName }
            .map { (appName, appLogs) -> AppBlockStat(appName, appLogs.size, appLogs.first().packageName) }
            .sortedByDescending { it.count }
            .take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Export format generators
    fun generateCsvExport(): String {
        val sb = StringBuilder()
        sb.append("ID,Title,Mode,DurationMinutes,ElapsedSeconds,Completed,Abandoned,StartTime,EndTime,StrictMode\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        allSessions.value.forEach { s ->
            sb.append("${s.id},\"${s.title}\",${s.mode},${s.durationMinutes},${s.elapsedSeconds},${s.completed},${s.abandoned},\"${sdf.format(Date(s.startTime))}\",\"${sdf.format(Date(s.endTime))}\",${s.isStrictMode}\n")
        }
        return sb.toString()
    }

    fun generateJsonExport(): String {
        val root = JSONObject()
        val sessionsArray = JSONArray()
        allSessions.value.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("mode", s.mode)
                put("durationMinutes", s.durationMinutes)
                put("elapsedSeconds", s.elapsedSeconds)
                put("completed", s.completed)
                put("abandoned", s.abandoned)
                put("startTime", s.startTime)
                put("endTime", s.endTime)
                put("isStrictMode", s.isStrictMode)
                put("notes", s.notes)
            }
            sessionsArray.put(obj)
        }
        root.put("sessions", sessionsArray)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("app", "FocusLock")
        return root.toString(2)
    }
}

data class DayFocusData(val dayLabel: String, val minutes: Int)
data class AppBlockStat(val appName: String, val count: Int, val packageName: String)
