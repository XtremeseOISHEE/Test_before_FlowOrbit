package com.example.floworbit.presentation.blockedapps

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floworbit.domain.model.InstalledApp
import com.example.floworbit.util.AppUtils
import com.example.floworbit.util.UsagePermissionHelper
import com.example.floworbit.util.OverlayPermissionHelper
import androidx.core.graphics.drawable.toBitmap

@Composable
fun BlockedAppsScreen(vm: BlockedAppsViewModel = viewModel()) {
    val context = LocalContext.current
    val apps by vm.apps.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select apps to block:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(modifier = Modifier.weight(1f), onClick = { AppUtils.openInstalledAppsSettings(context) }) {
                Text("Installed Apps")
            }
            Button(modifier = Modifier.weight(1f), onClick = {
                val intent = UsagePermissionHelper.requestUsageAccessIntent()
                if (context is Activity) context.startActivity(intent)
                else context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text("Usage Access") }
            Button(modifier = Modifier.weight(1f), onClick = {
                val intent = OverlayPermissionHelper.requestOverlayIntent(context.packageName)
                if (context is Activity) context.startActivity(intent)
                else context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text("Overlay") }
        }

        Spacer(Modifier.height(18.dp))

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(apps) { app ->
                AppItemRow(app = app, onToggle = { vm.toggleBlock(app) })
                Divider()
            }
        }
    }
}

@Composable
fun AppItemRow(app: InstalledApp, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            app.icon?.let {
                Image(bitmap = it.toBitmap().asImageBitmap(), contentDescription = app.appName, modifier = Modifier.size(42.dp))
                Spacer(Modifier.width(12.dp))
            }
            Column {
                Text(app.appName, style = MaterialTheme.typography.bodyLarge)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
            }
        }

        Switch(
            checked = app.isBlocked,
            onCheckedChange = { onToggle() },
            enabled = !app.packageName.equals(LocalContext.current.packageName) // own app always locked
        )
    }
}
