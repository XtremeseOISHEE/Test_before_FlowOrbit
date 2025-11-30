package com.example.floworbit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.floworbit.data.blockedapps.BlockedAppsRepository
import com.example.floworbit.data.violationlog.ViolationLog
import com.example.floworbit.data.violationlog.ViolationLogRepository
import com.example.floworbit.util.AllowanceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import android.util.Log
class AppTrackingService : Service() {

    companion object {
        private const val CHANNEL_ID = "app_tracking_channel"
        private const val NOTIF_ID = 901
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var blockedRepo: BlockedAppsRepository
    private lateinit var logRepo: ViolationLogRepository
    private var lastForeground: String? = null

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        blockedRepo = BlockedAppsRepository(applicationContext)
        logRepo = ViolationLogRepository(applicationContext)
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Tracking apps..."))
        startPolling()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "App tracking", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FlowOrbit — tracking")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()

    private fun startPolling() {
        serviceScope.launch {
            while (isActive) {
                val foreground = queryForegroundPackage()
                if (foreground != null && foreground != lastForeground) {
                    lastForeground = foreground

                    val blocked = blockedRepo.getBlockedApps().first()
                    val blockedApp = blocked.find { it.packageName == foreground }

                    if (blockedApp != null &&
                        !AllowanceManager.isAllowedNow(applicationContext, foreground)
                    ) {
                        startOverlay(foreground, blockedApp.label)

                        logRepo.addLog(
                            ViolationLog(
                                packageName = foreground,
                                label = blockedApp.label
                            )
                        )
                    }
                }
                delay(1000)
            }
        }
    }

    private fun startOverlay(pkg: String, label: String?) {
        val intent = Intent(this, WarningOverlayService::class.java).apply {
            putExtra("pkg", pkg)
            putExtra("label", label)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun queryForegroundPackage(): String? {
        return try {
            val now = System.currentTimeMillis()
            val begin = now - TimeUnit.SECONDS.toMillis(10)

            Log.d("FLOW", "Querying usage events: begin=$begin now=$now")

            val events = usageStatsManager.queryEvents(begin, now)
            var foreground: String? = null
            val ev = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                Log.d("FLOW", "Event: ${ev.eventType} pkg=${ev.packageName}")

                if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foreground = ev.packageName
                    Log.d("FLOW", "MOVE_TO_FOREGROUND -> $foreground")
                }
            }

            Log.d("FLOW", "Final foreground = $foreground")
            foreground

        } catch (e: SecurityException) {
            Log.e("FLOW", "Usage access missing OR query failed", e)
            null
        } catch (t: Throwable) {
            Log.e("FLOW", "queryForegroundPackage crashed", t)
            null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
