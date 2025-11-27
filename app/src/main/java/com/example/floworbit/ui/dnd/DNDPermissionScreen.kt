package com.example.floworbit.ui.dnd


import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floworbit.presentation.dnd.DNDViewModel
import androidx.navigation.NavController

@Composable
fun DNDPermissionScreen(
    navController: NavController,
    dndViewModel: DNDViewModel = viewModel()
) {
    val context = LocalContext.current
    val hasPermission by dndViewModel.hasPermission.collectAsState()

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            navController.navigate("home") {
                popUpTo("dnd_permission") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Allow Do Not Disturb (DND) Access",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("This is needed to automatically enable DND during Focus Mode.")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                )
            }
        ) {
            Text("Grant Permission")
        }
    }
}
