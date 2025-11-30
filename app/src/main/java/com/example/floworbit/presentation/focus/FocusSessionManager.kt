package com.example.floworbit.presentation.focus

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.floworbit.service.AppTrackingService
import com.example.floworbit.util.UsagePermissionHelper

object FocusSessionManager {

    fun startTracking(context: Context) {
        // 1️⃣ Check Usage Access permission
        if (!UsagePermissionHelper.hasUsageAccess(context)) {
            // Open system screen to grant permission
            context.startActivity(
                UsagePermissionHelper.requestUsageAccessIntent().apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            return // stop until user grants permission
        }

        // 2️⃣ Start AppTrackingService
        val i = Intent(context, AppTrackingService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, i)
        } else {
            context.startService(i)
        }
    }

    fun stopTracking(context: Context) {
        val i = Intent(context, AppTrackingService::class.java)
        context.stopService(i)
    }
}
