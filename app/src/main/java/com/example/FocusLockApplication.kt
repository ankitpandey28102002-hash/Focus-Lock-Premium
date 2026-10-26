package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.AppManager
import com.example.data.repository.FocusRepository
import com.example.data.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class FocusLockApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val preferenceRepository by lazy { PreferenceRepository(this) }
    val appManager by lazy { AppManager(this) }

    val repository by lazy {
        FocusRepository(
            sessionDao = database.focusSessionDao(),
            blockListDao = database.blockListDao(),
            profileDao = database.focusProfileDao(),
            scheduleDao = database.scheduleDao(),
            auditLogDao = database.blockAuditLogDao(),
            preferenceRepository = preferenceRepository
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}
