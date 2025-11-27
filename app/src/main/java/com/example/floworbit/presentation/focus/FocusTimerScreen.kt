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

        Spacer(modifier = Modifier.height(40.dp))

        if (!running) {
            Button(onClick = { focusViewModel.start(25) }) {
                Text("Start Focus (25 min)")
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
