package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.FocusLockApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class FocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return // Ignore self

        val app = applicationContext as? FocusLockApplication ?: return

        // 1. Check if active session is blocking this app
        val isBlockedBySession = FocusSessionService.isAppCurrentlyBlocked(packageName)

        if (isBlockedBySession) {
            handleBlockedApp(packageName, "FOCUS_SESSION", FocusSessionService.sessionState.value.title)
            return
        }

        // 2. Check active schedule
        serviceScope.launch {
            checkAndHandleScheduleBlocking(app, packageName)
        }
    }

    private suspend fun checkAndHandleScheduleBlocking(app: FocusLockApplication, packageName: String) {
        val now = Calendar.getInstance()
        val currentDay = when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val schedule = app.database.scheduleDao().getScheduleById(1)
        if (schedule != null && schedule.isEnabled && schedule.getDaysList().contains(currentDay)) {
            val startMin = schedule.startHour * 60 + schedule.startMinute
            val endMin = schedule.endHour * 60 + schedule.endMinute
            if (currentMinutes in startMin..endMin) {
                val blockList = schedule.blockListId?.let { app.database.blockListDao().getBlockListById(it) }
                if (blockList != null && blockList.getPackageList().contains(packageName)) {
                    handleBlockedApp(packageName, "SCHEDULE", schedule.name)
                }
            }
        }
    }

    private fun handleBlockedApp(packageName: String, reason: String, sessionTitle: String) {
        performGlobalAction(GLOBAL_ACTION_HOME)

        val app = applicationContext as FocusLockApplication
        val appName = app.appManager.getAppName(packageName)

        serviceScope.launch {
            app.repository.recordBlockAudit(packageName, appName, reason, sessionTitle)
        }

        val intent = Intent(this, FocusBlockedActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(FocusBlockedActivity.EXTRA_BLOCKED_APP, appName)
            putExtra(FocusBlockedActivity.EXTRA_BLOCKED_PKG, packageName)
            putExtra(FocusBlockedActivity.EXTRA_REASON, reason)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
    }
}
