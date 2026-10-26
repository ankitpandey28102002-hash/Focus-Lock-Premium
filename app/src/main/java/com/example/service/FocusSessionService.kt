package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.FocusLockApplication
import com.example.MainActivity
import com.example.R
import com.example.data.model.FocusMode
import com.example.data.model.FocusSessionEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FocusSessionService : Service() {

    companion object {
        const val CHANNEL_ID = "focus_session_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_FINISH = "ACTION_FINISH"

        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_MODE = "EXTRA_MODE"
        const val EXTRA_DURATION_MINS = "EXTRA_DURATION_MINS"
        const val EXTRA_IS_STRICT = "EXTRA_IS_STRICT"
        const val EXTRA_BLOCK_LIST_ID = "EXTRA_BLOCK_LIST_ID"

        data class ServiceState(
            val isRunning: Boolean = false,
            val isPaused: Boolean = false,
            val title: String = "",
            val mode: FocusMode = FocusMode.QUICK,
            val durationMinutes: Int = 25,
            val remainingSeconds: Int = 25 * 60,
            val elapsedSeconds: Int = 0,
            val isStrict: Boolean = false,
            val blockListId: Long? = null,
            val blockedPackages: Set<String> = emptySet(),
            val pausesCount: Int = 0,
            val startTime: Long = 0L
        )

        private val _sessionState = MutableStateFlow(ServiceState())
        val sessionState = _sessionState.asStateFlow()

        fun isAppCurrentlyBlocked(packageName: String): Boolean {
            val state = _sessionState.value
            return state.isRunning && state.blockedPackages.contains(packageName)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Focus Session"
                val modeStr = intent.getStringExtra(EXTRA_MODE) ?: FocusMode.QUICK.name
                val mode = try { FocusMode.valueOf(modeStr) } catch (e: Exception) { FocusMode.QUICK }
                val durationMins = intent.getIntExtra(EXTRA_DURATION_MINS, 25)
                val isStrict = intent.getBooleanExtra(EXTRA_IS_STRICT, false)
                val blockListId = intent.getLongExtra(EXTRA_BLOCK_LIST_ID, -1L).takeIf { it > 0 }

                startFocusSession(title, mode, durationMins, isStrict, blockListId)
            }
            ACTION_PAUSE -> pauseSession()
            ACTION_RESUME -> resumeSession()
            ACTION_FINISH -> finishSession(completed = false, abandoned = true)
        }
        return START_STICKY
    }

    private fun startFocusSession(
        title: String,
        mode: FocusMode,
        durationMins: Int,
        isStrict: Boolean,
        blockListId: Long?
    ) {
        val app = applicationContext as FocusLockApplication
        serviceScope.launch {
            val blockedPkgs: Set<String> = if (blockListId != null) {
                app.repository.getBlockListById(blockListId)?.getPackageList()?.toSet() ?: emptySet()
            } else {
                emptySet()
            }

            val startTime = System.currentTimeMillis()
            val totalSeconds = durationMins * 60

            _sessionState.value = ServiceState(
                isRunning = true,
                isPaused = false,
                title = title,
                mode = mode,
                durationMinutes = durationMins,
                remainingSeconds = totalSeconds,
                elapsedSeconds = 0,
                isStrict = isStrict,
                blockListId = blockListId,
                blockedPackages = blockedPkgs,
                pausesCount = 0,
                startTime = startTime
            )

            // Persist for recovery
            app.preferenceRepository.saveActiveSessionState(
                isRunning = true,
                startTime = startTime,
                endTime = startTime + (totalSeconds * 1000L),
                durationMins = durationMins,
                remainingSecs = totalSeconds,
                title = title,
                mode = mode.name,
                isStrict = isStrict,
                blockListId = blockListId,
                isPaused = false,
                pausesCount = 0
            )

            startForeground(NOTIFICATION_ID, buildNotification(title, totalSeconds, false))
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _sessionState.value
                if (!current.isRunning || current.isPaused) continue

                val newRemaining = current.remainingSeconds - 1
                val newElapsed = current.elapsedSeconds + 1

                if (newRemaining <= 0) {
                    _sessionState.value = current.copy(
                        remainingSeconds = 0,
                        elapsedSeconds = newElapsed,
                        isRunning = false
                    )
                    finishSession(completed = true, abandoned = false)
                    break
                } else {
                    _sessionState.value = current.copy(
                        remainingSeconds = newRemaining,
                        elapsedSeconds = newElapsed
                    )

                    // Update notification periodically
                    if (newElapsed % 5 == 0) {
                        val notification = buildNotification(current.title, newRemaining, current.isPaused)
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nm.notify(NOTIFICATION_ID, notification)
                    }
                }
            }
        }
    }

    private fun pauseSession() {
        val current = _sessionState.value
        if (!current.isRunning || current.isPaused || current.isStrict) return

        _sessionState.value = current.copy(
            isPaused = true,
            pausesCount = current.pausesCount + 1
        )
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(current.title, current.remainingSeconds, true))
    }

    private fun resumeSession() {
        val current = _sessionState.value
        if (!current.isRunning || !current.isPaused) return

        _sessionState.value = current.copy(isPaused = false)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(current.title, current.remainingSeconds, false))
    }

    private fun finishSession(completed: Boolean, abandoned: Boolean) {
        val current = _sessionState.value
        val app = applicationContext as FocusLockApplication

        serviceScope.launch {
            if (current.elapsedSeconds > 10) {
                app.repository.insertSession(
                    FocusSessionEntity(
                        title = current.title,
                        mode = current.mode.name,
                        durationMinutes = current.durationMinutes,
                        elapsedSeconds = current.elapsedSeconds,
                        completed = completed,
                        abandoned = abandoned,
                        startTime = current.startTime,
                        endTime = System.currentTimeMillis(),
                        blockListId = current.blockListId,
                        pausesCount = current.pausesCount,
                        isStrictMode = current.isStrict
                    )
                )
            }
            app.preferenceRepository.clearActiveSessionState()
            _sessionState.value = ServiceState(isRunning = false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildNotification(title: String, remainingSecs: Int, isPaused: Boolean): Notification {
        val mins = remainingSecs / 60
        val secs = remainingSecs % 60
        val timeFormatted = String.format("%02d:%02d", mins, secs)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isPaused) "Paused • $timeFormatted left" else "Focusing • $timeFormatted remaining"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🔒 $title")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FocusLock Active Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active countdown and blocking status during focus lock sessions."
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
