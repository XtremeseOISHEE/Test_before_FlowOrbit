package com.example.floworbit.util


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.view.WindowManager

object OverlayPermissionHelper {

    /** Checks if the app has "Display over other apps" permission */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // permission not required below M
        }
    }

    /** Intent to open the system overlay permission screen */
    fun requestOverlayIntent(packageName: String): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        } else {
            Intent() // no-op below M
        }
    }
}
