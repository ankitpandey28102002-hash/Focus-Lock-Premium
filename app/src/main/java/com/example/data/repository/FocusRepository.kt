package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FocusRepository(
    private val sessionDao: FocusSessionDao,
    private val blockListDao: BlockListDao,
    private val profileDao: FocusProfileDao,
    private val scheduleDao: ScheduleDao,
    private val auditLogDao: BlockAuditLogDao,
    val preferenceRepository: PreferenceRepository
) {
    val allSessions: Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()
    val completedSessions: Flow<List<FocusSessionEntity>> = sessionDao.getCompletedSessions()
    val allBlockLists: Flow<List<BlockListEntity>> = blockListDao.getAllBlockLists()
    val allProfiles: Flow<List<FocusProfileEntity>> = profileDao.getAllProfiles()
    val allSchedules: Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()
    val recentAuditLogs: Flow<List<BlockAuditLogEntity>> = auditLogDao.getRecentLogs(100)

    // Today's focused time in minutes
    val todayFocusedMinutes: Flow<Int> = sessionDao.getAllSessions().map { sessions ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis

        sessions
            .filter { it.startTime in startOfDay..endOfDay && (it.completed || it.elapsedSeconds > 60) }
            .sumOf { it.elapsedSeconds } / 60
    }

    // Current Streak calculation
    val focusStreakDays: Flow<Int> = sessionDao.getAllSessions().map { sessions ->
        val completed = sessions.filter { it.completed && it.elapsedSeconds >= 300 }
        if (completed.isEmpty()) return@map 0

        val cal = Calendar.getInstance()
        var streak = 0
        var checkDate = Calendar.getInstance()

        // Check if user did session today or yesterday to maintain active streak
        val sessionDays = completed.map {
            val sCal = Calendar.getInstance().apply { timeInMillis = it.startTime }
            "${sCal.get(Calendar.YEAR)}-${sCal.get(Calendar.DAY_OF_YEAR)}"
        }.toSet()

        val todayKey = "${checkDate.get(Calendar.YEAR)}-${checkDate.get(Calendar.DAY_OF_YEAR)}"
        checkDate.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = "${checkDate.get(Calendar.YEAR)}-${checkDate.get(Calendar.DAY_OF_YEAR)}"

        var currentDayCal = Calendar.getInstance()
        if (sessionDays.contains(todayKey)) {
            streak = 1
            currentDayCal.add(Calendar.DAY_OF_YEAR, -1)
        } else if (sessionDays.contains(yesterdayKey)) {
            streak = 0 // Will count from yesterday backwards
        } else {
            return@map 0
        }

        while (true) {
            val key = "${currentDayCal.get(Calendar.YEAR)}-${currentDayCal.get(Calendar.DAY_OF_YEAR)}"
            if (sessionDays.contains(key)) {
                streak++
                currentDayCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        streak
    }

    suspend fun insertSession(session: FocusSessionEntity): Long = sessionDao.insertSession(session)
    suspend fun updateSession(session: FocusSessionEntity) = sessionDao.updateSession(session)
    suspend fun deleteSession(session: FocusSessionEntity) = sessionDao.deleteSession(session)

    suspend fun insertBlockList(blockList: BlockListEntity): Long = blockListDao.insertBlockList(blockList)
    suspend fun updateBlockList(blockList: BlockListEntity) = blockListDao.updateBlockList(blockList)
    suspend fun deleteBlockList(blockList: BlockListEntity) = blockListDao.deleteBlockList(blockList)
    suspend fun getBlockListById(id: Long): BlockListEntity? = blockListDao.getBlockListById(id)

    suspend fun insertProfile(profile: FocusProfileEntity): Long = profileDao.insertProfile(profile)
    suspend fun updateProfile(profile: FocusProfileEntity) = profileDao.updateProfile(profile)
    suspend fun deleteProfile(profile: FocusProfileEntity) = profileDao.deleteProfile(profile)

    suspend fun insertSchedule(schedule: ScheduleEntity): Long = scheduleDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: ScheduleEntity) = scheduleDao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleEntity) = scheduleDao.deleteSchedule(schedule)

    suspend fun recordBlockAudit(packageName: String, appName: String, reason: String, sessionTitle: String) {
        auditLogDao.insertLog(
            BlockAuditLogEntity(
                packageName = packageName,
                appName = appName,
                blockReason = reason,
                sessionTitle = sessionTitle
            )
        )
    }

    // Seed realistic demo data when requested by user in Developer/Testing mode
    suspend fun seedDemoData() {
        val now = System.currentTimeMillis()
        val oneHour = 3600 * 1000L
        val oneDay = 24 * 3600 * 1000L

        val demoSessions = listOf(
            FocusSessionEntity(
                title = "Deep Android Architecture",
                mode = FocusMode.DEEP_FOCUS.name,
                durationMinutes = 60,
                elapsedSeconds = 3600,
                completed = true,
                abandoned = false,
                startTime = now - (oneHour * 2),
                endTime = now - oneHour,
                notes = "Finished database integration and tests without distraction.",
                isStrictMode = false
            ),
            FocusSessionEntity(
                title = "Exam Revision Sprint",
                mode = FocusMode.STUDY.name,
                durationMinutes = 45,
                elapsedSeconds = 2700,
                completed = true,
                abandoned = false,
                startTime = now - (oneHour * 6),
                endTime = now - (oneHour * 5) - (15 * 60 * 1000L),
                notes = "Pomodoro intervals completed smoothly.",
                isStrictMode = true
            ),
            FocusSessionEntity(
                title = "Morning Deep Work",
                mode = FocusMode.WORK.name,
                durationMinutes = 50,
                elapsedSeconds = 3000,
                completed = true,
                abandoned = false,
                startTime = now - oneDay - (oneHour * 3),
                endTime = now - oneDay - (oneHour * 2),
                notes = "Zero app interruptions.",
                isStrictMode = false
            ),
            FocusSessionEntity(
                title = "Night Digital Detox",
                mode = FocusMode.SLEEP.name,
                durationMinutes = 30,
                elapsedSeconds = 1800,
                completed = true,
                abandoned = false,
                startTime = now - (oneDay * 2) - (oneHour * 8),
                endTime = now - (oneDay * 2) - (oneHour * 7) - (30 * 60 * 1000L),
                notes = "Screen locked before bed.",
                isStrictMode = true
            ),
            FocusSessionEntity(
                title = "Quick Coding Sprint",
                mode = FocusMode.QUICK.name,
                durationMinutes = 20,
                elapsedSeconds = 900,
                completed = false,
                abandoned = true,
                startTime = now - (oneDay * 3) - (oneHour * 4),
                endTime = now - (oneDay * 3) - (oneHour * 3) - (45 * 60 * 1000L),
                notes = "Urgent phone call interrupted.",
                isStrictMode = false
            )
        )

        demoSessions.forEach { sessionDao.insertSession(it) }

        // Seed sample audit logs
        recordBlockAudit("com.instagram.android", "Instagram", "SESSION", "Deep Android Architecture")
        recordBlockAudit("com.zhiliaoapp.musically", "TikTok", "SESSION", "Exam Revision Sprint")
        recordBlockAudit("com.twitter.android", "X (Twitter)", "SCHEDULE", "Morning Deep Work")

        preferenceRepository.setDemoDataLoaded(true)
    }

    suspend fun clearAllData() {
        sessionDao.deleteAll()
        auditLogDao.deleteAll()
        preferenceRepository.clearActiveSessionState()
        preferenceRepository.setDemoDataLoaded(false)
    }
}
