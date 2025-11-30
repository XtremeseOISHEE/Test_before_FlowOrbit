package com.example.floworbit

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.rememberNavController
import com.example.floworbit.navigation.NavGraph
import com.example.floworbit.service.AppTrackingService
import com.example.floworbit.ui.theme.FlowOrbitTheme

class MainActivity : ComponentActivity() {

    // Launcher for Usage Access permission
    private val requestUsageAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasUsageAccessPermission()) {
            requestOverlayPermission()
        } else {
            Toast.makeText(
                this,
                "Usage Access is required for FlowOrbit.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Launcher for Overlay (Draw over other apps) permission
    private val requestOverlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasOverlayPermission()) {
            startTrackingService()
        } else {
            Toast.makeText(
                this,
                "Overlay permission is required to show warnings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FlowOrbitTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }

        // Start permission flow
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        when {
            !hasUsageAccessPermission() -> requestUsageAccessPermission()
            !hasOverlayPermission() -> requestOverlayPermission()
            else -> startTrackingService()
        }
    }

    private fun hasUsageAccessPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        requestUsageAccessLauncher.launch(intent)
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            requestOverlayPermissionLauncher.launch(intent)
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, AppTrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}