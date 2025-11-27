package com.example.floworbit.presentation.focus

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floworbit.presentation.dnd.DNDViewModel
import com.example.floworbit.util.formatMillis

@Composable
fun FocusTimerScreen(
    app: Application = LocalContext.current.applicationContext as Application
) {
    // First: DND ViewModel
    val dndViewModel: DNDViewModel = viewModel()

    // Second: Focus ViewModel using factory
    val focusViewModel: FocusTimerViewModel = viewModel(
        factory = FocusTimerViewModelFactory(app, dndViewModel)
    )

    val remaining by focusViewModel.remaining.collectAsState()
    val running by focusViewModel.running.collectAsState()

    // 🔥 NEW — for custom minutes
    var customMinutes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatMillis(remaining),
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 🔥 NEW — Custom timer input (only visible when NOT running)
        if (!running) {
            OutlinedTextField(
                value = customMinutes,
                onValueChange = { customMinutes = it.filter { c -> c.isDigit() } },
                label = { Text("Custom Minutes") },
                singleLine = true,
                modifier = Modifier.width(180.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Buttons
        if (!running) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Default Pomodoro
                Button(onClick = { focusViewModel.start(25) }) {
                    Text("Start Focus (25 min)")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Start button
                Button(
                    onClick = {
                        val minutes = customMinutes.toIntOrNull() ?: 0
                        if (minutes > 0) {
                            focusViewModel.start(minutes)
                        }
                    }
                ) {
                    Text("Start Custom")
                }
            }
        } else {
            Button(onClick = { focusViewModel.pause() }) {
                Text("Pause")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = { focusViewModel.stop() }) {
                Text("Stop")
            }
        }
    }
}
