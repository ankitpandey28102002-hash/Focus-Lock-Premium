package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startOfDay AND startTime <= :endOfDay ORDER BY startTime DESC")
    fun getSessionsForDay(startOfDay: Long, endOfDay: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE completed = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): FocusSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAll()
}

@Dao
interface BlockListDao {
    @Query("SELECT * FROM block_lists ORDER BY isSystemDefault DESC, name ASC")
    fun getAllBlockLists(): Flow<List<BlockListEntity>>

    @Query("SELECT * FROM block_lists WHERE id = :id LIMIT 1")
    suspend fun getBlockListById(id: Long): BlockListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockList(blockList: BlockListEntity): Long

    @Update
    suspend fun updateBlockList(blockList: BlockListEntity)

    @Delete
    suspend fun deleteBlockList(blockList: BlockListEntity)

    @Query("DELETE FROM block_lists")
    suspend fun deleteAll()
}

@Dao
interface FocusProfileDao {
    @Query("SELECT * FROM focus_profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<FocusProfileEntity>>

    @Query("SELECT * FROM focus_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): FocusProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: FocusProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: FocusProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: FocusProfileEntity)

    @Query("DELETE FROM focus_profiles")
    suspend fun deleteAll()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY startHour ASC, startMinute ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE isEnabled = 1")
    fun getActiveSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
}

@Dao
interface BlockAuditLogDao {
    @Query("SELECT * FROM block_audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<BlockAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BlockAuditLogEntity): Long

    @Query("DELETE FROM block_audit_logs")
    suspend fun deleteAll()
}
