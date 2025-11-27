package com.example.floworbit.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build

class DNDManager(private val context: Context) {

    // System Services
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /**
     * Check if app has permission to modify DND mode
     */
    fun hasAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Enable Do Not Disturb Mode (Silent)
     */
    fun enableDND() {
        if (!hasAccess()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        } else {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        }
    }

    /**
     * Disable DND mode → return to normal
     */
    fun disableDND() {
        if (!hasAccess()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        } else {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
    }
}
