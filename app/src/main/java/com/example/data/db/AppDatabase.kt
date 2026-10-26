package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FocusSessionEntity::class,
        BlockListEntity::class,
        FocusProfileEntity::class,
        ScheduleEntity::class,
        BlockAuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun blockListDao(): BlockListDao
    abstract fun focusProfileDao(): FocusProfileDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun blockAuditLogDao(): BlockAuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focuslock_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: AppDatabase) {
                val blockListDao = db.blockListDao()
                val profileDao = db.focusProfileDao()
                val scheduleDao = db.scheduleDao()

                // Default Block Lists
                val socialListId = blockListDao.insertBlockList(
                    BlockListEntity(
                        name = "Social Media Detox",
                        icon = "Share",
                        description = "Blocks high-distraction social feeds, short video reels, and message boards",
                        packages = "com.instagram.android,com.facebook.katana,com.zhiliaoapp.musically,com.twitter.android,com.snapchat.android,com.reddit.frontpage",
                        isSystemDefault = true
                    )
                )

                val workListId = blockListDao.insertBlockList(
                    BlockListEntity(
                        name = "Deep Work & Study",
                        icon = "Work",
                        description = "Blocks all entertainment, media streamers, gaming, and shopping apps",
                        packages = "com.google.android.youtube,com.netflix.mediaclient,com.spotify.music,com.amazon.mShop.android.shopping,com.discord",
                        isSystemDefault = true
                    )
                )

                blockListDao.insertBlockList(
                    BlockListEntity(
                        name = "Gaming & Arcade",
                        icon = "SportsEsports",
                        description = "Locks mobile games and streaming clients during study hours",
                        packages = "com.supercell.clashroyale,com.roblox.client,com.ea.gp.fifamobile,com.mojang.minecraftpe",
                        isSystemDefault = true
                    )
                )

                blockListDao.insertBlockList(
                    BlockListEntity(
                        name = "Bedtime Silence",
                        icon = "Bedtime",
                        description = "Comprehensive evening block of all late-night dopamine triggers",
                        packages = "com.google.android.youtube,com.instagram.android,com.twitter.android,com.netflix.mediaclient,com.reddit.frontpage",
                        isSystemDefault = true
                    )
                )

                // Default Profiles
                profileDao.insertProfile(
                    FocusProfileEntity(
                        name = "Deep Coding Sprint",
                        icon = "Terminal",
                        mode = FocusMode.DEEP_FOCUS.name,
                        defaultDurationMinutes = 50,
                        breakMinutes = 10,
                        longBreakMinutes = 20,
                        isStrict = false,
                        blockListId = workListId
                    )
                )

                profileDao.insertProfile(
                    FocusProfileEntity(
                        name = "Strict Study Lock",
                        icon = "Lock",
                        mode = FocusMode.STRICT.name,
                        defaultDurationMinutes = 45,
                        breakMinutes = 5,
                        longBreakMinutes = 15,
                        isStrict = true,
                        blockListId = socialListId
                    )
                )

                profileDao.insertProfile(
                    FocusProfileEntity(
                        name = "Standard Pomodoro",
                        icon = "Timer",
                        mode = FocusMode.POMODORO.name,
                        defaultDurationMinutes = 25,
                        breakMinutes = 5,
                        longBreakMinutes = 15,
                        isStrict = false,
                        blockListId = socialListId
                    )
                )

                // Default Schedule
                scheduleDao.insertSchedule(
                    ScheduleEntity(
                        name = "Morning Deep Work",
                        icon = "WbSunny",
                        startHour = 9,
                        startMinute = 0,
                        endHour = 12,
                        endMinute = 0,
                        daysOfWeek = "1,2,3,4,5",
                        blockListId = workListId,
                        isEnabled = false,
                        isStrict = false,
                        notificationBeforeMinutes = 10
                    )
                )
            }
        }
    }
}
