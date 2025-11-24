package com.example.floworbit.util



import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object TimerNotification {

    const val CHANNEL_ID = "focus_timer_channel"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
