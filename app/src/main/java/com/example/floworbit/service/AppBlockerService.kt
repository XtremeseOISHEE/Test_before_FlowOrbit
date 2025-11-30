package com.example.floworbit.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.floworbit.data.blockedapps.BlockedAppsRepository
import com.example.floworbit.presentation.blockedapps.BlockActivity
import com.example.floworbit.util.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppBlockerService : AccessibilityService() {

    private lateinit var repo: BlockedAppsRepository

    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = BlockedAppsRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We only care about window / view events that indicate a package moved to foreground.
        val pkgName = event?.packageName?.toString() ?: return

        // Ignore safe system apps and our own package
        if (AppUtils.isSafeSystemApp(pkgName) || pkgName == packageName) return

        // Check DB on background thread
        CoroutineScope(Dispatchers.IO).launch {
            val blockedPackages = repo.getBlockedPackageNames() // suspend
            if (blockedPackages.contains(pkgName)) {
                // Launch the BlockActivity (composable Compose Activity)
                val intent = Intent(this@AppBlockerService, BlockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("packageName", pkgName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // nothing special
    }
}
