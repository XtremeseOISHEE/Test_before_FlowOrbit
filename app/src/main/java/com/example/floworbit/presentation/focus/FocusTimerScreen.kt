package com.example.floworbit.presentation.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.floworbit.domain.model.FocusSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    navController: NavController,
    viewModel: FocusTimerViewModel = viewModel()
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val remaining by viewModel.remainingMillis.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    var customMinutesText by remember { mutableStateOf("25") } // default Pomodoro

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Focus Mode") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Timer"
                    )
                },
                text = { Text("Home") },
                onClick = { navController.navigate("home") },
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Remaining", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = viewModel.formatMillisToClock(remaining),
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Presets: Pomodoro / 45 / Custom
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.start(25) }, enabled = !isRunning) { Text("25m") }
                Button(onClick = { viewModel.start(45) }, enabled = !isRunning) { Text("45m") }
                OutlinedTextField(
                    value = customMinutesText,
                    onValueChange = { customMinutesText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Minutes") },
                    modifier = Modifier.width(120.dp)
                )
                Button(
                    onClick = {
                        val minutes = customMinutesText.toIntOrNull() ?: 0
                        if (minutes > 0) viewModel.start(minutes)
                    },
                    enabled = !isRunning
                ) {
                    Text("Start")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controls: Pause/Resume/Reset/Force Stop
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isRunning) {
                    Button(onClick = { viewModel.pause() }) { Text("Pause") }
                } else {
                    Button(onClick = { viewModel.resume() }) { Text("Resume") }
                }
                Button(onClick = { viewModel.reset() }) { Text("Reset") }
                Button(onClick = { viewModel.forceStop() }) { Text("Force Stop") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Session summary (last 5)
            Text("Recent sessions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (sessions.isEmpty()) {
                Text("No sessions yet")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val recent = sessions.takeLast(5).reversed()
                    recent.forEach { s -> SessionRow(s) }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: FocusSession) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Duration: ${formatMillis(session.durationMillis)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Interrupted: ${session.interrupted}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                formatTimeRange(session.startTimeMillis, session.endTimeMillis),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatMillis(ms: Long): String {
    val s = ms / 1000
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}

private fun formatTimeRange(start: Long, end: Long): String {
    val st = java.text.SimpleDateFormat("HH:mm").format(java.util.Date(start))
    val en = java.text.SimpleDateFormat("HH:mm").format(java.util.Date(end))
    return "$st - $en"
}
