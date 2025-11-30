package com.example.floworbit.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.floworbit.MainActivity
import com.example.floworbit.util.DNDManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FocusTimerService : LifecycleService() {

    companion object {
        const val CHANNEL_ID = "focus_timer_channel"
        const val NOTIFICATION_ID = 101

        const val ACTION_START = "FOCUS_START"
        const val ACTION_PAUSE = "FOCUS_PAUSE"
        const val ACTION_STOP = "FOCUS_STOP"

        const val EXTRA_DURATION = "duration"

        private val _remaining = MutableStateFlow(0L)
        val remainingTime = _remaining.asStateFlow()

        private val _running = MutableStateFlow(false)
        val isRunning = _running.asStateFlow()
    }

    private var timerJob: Job? = null
    private lateinit var dndManager: DNDManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        dndManager = DNDManager(this)
    }

    // In FocusTimerService.kt

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                // ⭐ THIS IS THE FIX ⭐
                // Check if the timer is currently paused.
                val isResuming = !_running.value && _remaining.value > 0

                if (isResuming) {
                    // If resuming, just call startTimer with the time we already have.
                    startTimer(_remaining.value)
                } else {
                    // Otherwise, start a brand new session with the duration from the intent.
                    val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
                    startTimer(duration)
                }
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }

        return START_STICKY
    }


    private fun startTimer(durationMillis: Long) {
        timerJob?.cancel()

        _remaining.value = durationMillis
        _running.value = true

        val notification = buildNotification(formatTime(durationMillis))

        // Android 14 fix
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // FIX: updated function name
        if (dndManager.hasAccess()) {
            dndManager.enableDND()
        }

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (_running.value && _remaining.value > 0) {
                delay(1000)
                _remaining.value -= 1000

                updateNotification(formatTime(_remaining.value))

                if (_remaining.value <= 0) finishSession()
            }
        }
    }

    private fun pauseTimer() {
        _running.value = false
        timerJob?.cancel()
        updateNotification("Paused: ${formatTime(_remaining.value)}")
    }

    private fun stopTimer() {
        _running.value = false
        timerJob?.cancel()
        _remaining.value = 0L

        // FIX
        if (dndManager.hasAccess()) {
            dndManager.disableDND()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun finishSession() {
        _running.value = false
        timerJob?.cancel()

        updateNotification("Completed!")

        // FIX
        if (dndManager.hasAccess()) {
            dndManager.disableDND()
        }

        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun buildNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Timer")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // FIX: correct signature
    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
