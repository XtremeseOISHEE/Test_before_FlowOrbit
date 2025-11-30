package com.example.floworbit.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import com.example.floworbit.domain.model.InstalledApp

object AppUtils {

    // Opens Android's "Apps" settings page
    fun openInstalledAppsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Return user-installed + launchable apps only.
     * Excludes core system packages and your own package by default.
     */
    fun getInstalledApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val ownPackage = context.packageName

        // add additional packages to exclude if you want
        val excludedPackages = setOf(
            "com.android.settings",   // Settings
            "com.android.vending",    // Play Store (optional)
            ownPackage                // exclude self so UI can still show it as locked if you want
        )

        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { appInfo ->
                val pkg = appInfo.packageName

                // Consider it a user app if FLAG_SYSTEM is NOT set.
                val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0

                // Make sure it has a launch intent
                val isLaunchable = pm.getLaunchIntentForPackage(pkg) != null

                isUserApp && isLaunchable && !excludedPackages.contains(pkg)
            }
            .map { appInfo ->
                InstalledApp(
                    appName = appInfo.loadLabel(pm).toString(),
                    packageName = appInfo.packageName,
                    icon = runCatching { appInfo.loadIcon(pm) }.getOrNull(),
                    isBlocked = false
                )
            }
            .sortedBy { it.appName.lowercase() }
            .toList()
    }

    /**
     * Small whitelist of system packages we never want to block.
     * Useful for Accessibility-based blocking to avoid launching for launchers, system UI, keyboards, etc.
     */
    fun isSafeSystemApp(pkg: String): Boolean {
        val safeApps = listOf(
            "com.android.systemui",
            "com.android.settings",
            "com.google.android.packageinstaller",
            "com.google.android.gms",
            "com.android.launcher",
            "com.google.android.inputmethod.latin", // gboard
            "com.android.vending" // play store (optional)
        )
        return safeApps.any { pkg.contains(it, ignoreCase = true) }
    }
}
