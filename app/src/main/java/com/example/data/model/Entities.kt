package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mode: String, // FocusMode.name
    val durationMinutes: Int,
    val elapsedSeconds: Int,
    val completed: Boolean,
    val abandoned: Boolean,
    val startTime: Long,
    val endTime: Long,
    val notes: String = "",
    val blockListId: Long? = null,
    val pausesCount: Int = 0,
    val isStrictMode: Boolean = false
)

@Entity(tableName = "block_lists")
data class BlockListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "Shield",
    val description: String = "",
    val packages: String = "", // Comma-separated package names
    val isSystemDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getPackageList(): List<String> {
        return if (packages.isBlank()) emptyList() else packages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

@Entity(tableName = "focus_profiles")
data class FocusProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "Star",
    val mode: String = FocusMode.QUICK.name,
    val defaultDurationMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val isStrict: Boolean = false,
    val blockListId: Long? = null,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "Schedule",
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val daysOfWeek: String = "1,2,3,4,5", // 1=Mon, 7=Sun
    val blockListId: Long? = null,
    val isEnabled: Boolean = true,
    val isStrict: Boolean = false,
    val notificationBeforeMinutes: Int = 5
) {
    fun getDaysList(): List<Int> {
        return if (daysOfWeek.isBlank()) emptyList() else daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}

@Entity(tableName = "block_audit_logs")
data class BlockAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val appName: String,
    val blockReason: String, // "SESSION", "SCHEDULE", "STRICT"
    val sessionTitle: String = ""
)
