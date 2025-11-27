package com.example.floworbit.ui.settings

import android.app.NotificationManager
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun DNDPermissionButton() {
    val context = LocalContext.current

    Button(
        onClick = {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            )
        }
    ) {
        Text("Enable DND Access")
    }
}
