package com.example.floworbit.presentation.focus

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floworbit.presentation.dnd.DNDViewModel
import com.example.floworbit.util.formatMillis

// This enum needs to be accessible by this file.
// If it's not already here or imported, it should be.


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    onBack: () -> Unit // Assumes onBack is provided from NavGraph
) {
    val app = LocalContext.current.applicationContext as Application
    val dndViewModel: DNDViewModel = viewModel()

    // This was the line that had the potential to cause issues.
    // The factory must be correctly defined elsewhere and accessible.
    val focusViewModel: FocusTimerViewModel = viewModel(
        factory = FocusTimerViewModelFactory(app, dndViewModel)
    )

    val remainingMillis by focusViewModel.remaining.collectAsState()
    val isRunning by focusViewModel.running.collectAsState()

    val timerState = when {
        isRunning -> TimerState.RUNNING
        remainingMillis > 0 && !isRunning -> TimerState.PAUSED
        else -> TimerState.STOPPED
    }

    var customMinutes by remember { mutableStateOf("25") }

    val totalTimeMillis = (customMinutes.toLongOrNull() ?: 25) * 60 * 1000
    val progress = if (totalTimeMillis > 0) {
        remainingMillis.toFloat() / totalTimeMillis.toFloat()
    } else {
        1f
    }

    // ⭐ THE FIX IS HERE ⭐
    // Get the color values OUTSIDE the Canvas block
    val trackColor = MaterialTheme.colorScheme.surfaceContainer
    val progressColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Now, use the simple color variables inside the DrawScope
                    drawArc(
                        color = trackColor, // <-- Use the variable
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Butt)
                    )
                    drawArc(
                        color = progressColor, // <-- Use the variable
                        startAngle = -90f,
                        sweepAngle = 360 * progress,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = formatMillis(remainingMillis),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Light,
                    color = textColor // <-- Use the variable
                )
            }

            if (timerState == TimerState.STOPPED) {
                OutlinedTextField(
                    value = customMinutes,
                    onValueChange = { customMinutes = it.filter { c -> c.isDigit() } },
                    label = { Text("Focus Duration (Minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(240.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (timerState != TimerState.STOPPED) {
                    OutlinedButton(
                        onClick = { focusViewModel.stop() },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape
                    ) {
                        Text("Stop")
                    }
                }

                Button(
                    onClick = {
                        when (timerState) {
                            TimerState.STOPPED -> {
                                val minutes = customMinutes.toIntOrNull() ?: 25
                                focusViewModel.start(minutes)
                            }
                            TimerState.PAUSED -> focusViewModel.start(0)
                            TimerState.RUNNING -> focusViewModel.pause()
                        }
                    },
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    enabled = !(timerState == TimerState.STOPPED && (customMinutes.toIntOrNull() ?: 0) <= 0)
                ) {
                    val buttonText = when (timerState) {
                        TimerState.RUNNING -> "Pause"
                        TimerState.PAUSED -> "Resume"
                        TimerState.STOPPED -> "Start"
                    }
                    Text(buttonText, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

// Ensure this factory class exists in its own file as you've mentioned
// This is just for reference
// class FocusTimerViewModelFactory(...) { ... }
