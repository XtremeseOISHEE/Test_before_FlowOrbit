package com.example.floworbit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import com.example.floworbit.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FocusTimerService : Service() {

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

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
                startTimer(duration)
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

        // ✅ ANDROID 14 FIX — MUST PASS foregroundServiceType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (_running.value && _remaining.value > 0) {
                delay(1000)
                _remaining.value -= 1000

                updateNotification(formatTime(_remaining.value))

                if (_remaining.value <= 0) {
                    finishSession()
                }
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

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun finishSession() {
        _running.value = false
        timerJob?.cancel()

        updateNotification("Completed!")

        // keep notification visible
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

    override fun onBind(intent: Intent?): IBinder? = null

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return "%02d:%02d".format(minutes, seconds)
    }
}
