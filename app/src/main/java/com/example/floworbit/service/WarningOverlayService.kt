package com.example.floworbit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.floworbit.util.AllowanceManager
import com.example.floworbit.util.OverlayPermissionHelper

class WarningOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "warning_overlay_channel"
        private const val NOTIF_ID = 222
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()

        // Foreground notification so Android allows overlay service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(ch)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FlowOrbit")
            .setContentText("Showing focus overlay")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pkg = intent?.getStringExtra("pkg") ?: ""
        val label = intent?.getStringExtra("label") ?: pkg

        if (!OverlayPermissionHelper.hasOverlayPermission(this)) {
            startActivity(
                OverlayPermissionHelper.requestOverlayIntent(packageName).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            stopSelf()
            return START_NOT_STICKY
        }

        showOverlay(pkg, label)
        return START_NOT_STICKY
    }

    private fun showOverlay(packageName: String, label: String) {
        val tv = TextView(this).apply {
            text = "You opened $label\nStay focused?"
            setTextColor(ContextCompat.getColor(this@WarningOverlayService, android.R.color.white))
            textSize = 20f
            setPadding(40, 40, 40, 40)
        }

        val btnReturn = Button(this).apply { text = "Return to Focus" }
        val btnContinue = Button(this).apply { text = "Allow 5 min" }
        val btnGoHome = Button(this).apply { text = "Go Home (Close App)" }

        val container = FrameLayout(this).apply {
            setBackgroundColor(0xDD000000.toInt())
            addView(tv)
            addView(btnReturn)
            addView(btnContinue)
            addView(btnGoHome)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = container

        windowManager?.addView(container, params)

        btnReturn.setOnClickListener { removeOverlay() }

        btnContinue.setOnClickListener {
            val until = System.currentTimeMillis() + 5 * 60 * 1000L
            AllowanceManager.setAllowanceUntil(this, packageName, until)
            removeOverlay()
        }

        btnGoHome.setOnClickListener {
            val home = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(home)
            AllowanceManager.clearAllowance(this, packageName)
            removeOverlay()
        }
    }

    private fun removeOverlay() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {}

        overlayView = null
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}
