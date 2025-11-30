package com.example.floworbit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.app.usage.UsageStatsManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.floworbit.data.blockedapps.BlockedAppsRepository
import com.example.floworbit.data.violationlog.ViolationLog
import com.example.floworbit.data.violationlog.ViolationLogRepository
import kotlinx.coroutines.*

class AppTrackingService : Service() {

    companion object {
        private const val CHANNEL_ID = "app_tracking_channel"
        private const val NOTIF_ID = 901
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var blockedRepo: BlockedAppsRepository
    private lateinit var logRepo: ViolationLogRepository

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        blockedRepo = BlockedAppsRepository(applicationContext)
        logRepo = ViolationLogRepository(applicationContext)

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Tracking apps..."))

        startPollingForAppChanges()
        observeAllowanceChanges()
    }

    /**
     * This coroutine REACTS to the allowance timer expiring.
     * When it ends, if the user is still in the app whose time expired, it closes the app.
     */
    private fun observeAllowanceChanges() {
        serviceScope.launch {
            var previousState: String? = AllowanceState.allowedPackage.value
            AllowanceState.allowedPackage.collect { newState ->
                // An allowance ends when the state changes from a package name to null.
                if (previousState != null && newState == null) {
                    Log.d("FLOW", "Allowance for '$previousState' has ended.")

                    val appThatWasAllowed = previousState!!
                    val currentApp = queryForegroundPackage()

                    // If the user is STILL on the app whose time just ran out, close it.
                    if (currentApp == appThatWasAllowed) {
                        val isFocusSessionRunning = FocusTimerService.isRunning.value
                        val isAppBlocked = blockedRepo.isAppBlocked(currentApp)
                        if (isFocusSessionRunning && isAppBlocked) {
                            Log.d("FLOW", "Closing '$currentApp' because its specific time limit expired.")
                            closeAppAndGoHome()
                        }
                    }
                }
                previousState = newState
            }
        }
    }

    /**
     * Sends an intent to return to the Android Home Screen.
     */
    private fun closeAppAndGoHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(homeIntent)
    }

    /**
     * This coroutine POLLS for changes in the foreground app.
     * It handles the case where a user switches TO a blocked app.
     */
    private fun startPollingForAppChanges() {
        serviceScope.launch {
            var lastForegroundPackage: String? = null
            while (isActive) {
                val currentForegroundPackage = queryForegroundPackage()

                if (currentForegroundPackage != null && currentForegroundPackage != lastForegroundPackage) {
                    val isFocusSessionRunning = FocusTimerService.isRunning.value
                    val isAppBlocked = blockedRepo.isAppBlocked(currentForegroundPackage)

                    // ⭐ Check if the current app is the one SPECIFICALLY allowed.
                    val isSpecificallyAllowed = AllowanceState.allowedPackage.value == currentForegroundPackage

                    // Block if focus is on, app is blocked, and it's NOT the specifically allowed one.
                    if (isFocusSessionRunning && isAppBlocked && !isSpecificallyAllowed) {
                        showBlockOverlayFor(currentForegroundPackage)
                    }
                    lastForegroundPackage = currentForegroundPackage
                }
                delay(1000) // Check every second
            }
        }
    }

    /**
     * Helper function to show the warning overlay.
     */
    private suspend fun showBlockOverlayFor(packageName: String) {
        val appLabel = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        logRepo.insert(ViolationLog(packageName = packageName, label = appLabel))

        val intent = Intent(this@AppTrackingService, WarningOverlayService::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("pkg", packageName)
            putExtra("label", appLabel)
        }
        startService(intent)
    }

    /**
     * Finds the app that was most recently in the foreground using UsageStatsManager.
     */
    private fun queryForegroundPackage(): String? {
        return try {
            val time = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 60 * 1000, time)
            stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (e: Exception) {
            Log.e("FLOW", "Could not query foreground package", e)
            null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "App tracking", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification = 
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FlowOrbit — tracking")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}