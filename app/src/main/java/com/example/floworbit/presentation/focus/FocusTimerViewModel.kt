package com.example.floworbit.presentation.focus

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.presentation.dnd.DNDViewModel
import com.example.floworbit.service.FocusTimerService
// This import must match your project structure
import com.example.floworbit.presentation.focus.FocusSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// This enum must be defined here for the UI to use
enum class TimerState {
    RUNNING,
    PAUSED,
    STOPPED
}

class FocusTimerViewModel(
    app: Application,
    private val dndViewModel: DNDViewModel
) : AndroidViewModel(app) {


    private val _remaining = MutableStateFlow(0L)
    val remaining = _remaining.asStateFlow()

    private val _running = MutableStateFlow(false)
    val running = _running.asStateFlow()

    init {
        viewModelScope.launch {
            FocusTimerService.remainingTime.collect { _remaining.value = it }
        }

        viewModelScope.launch {
            FocusTimerService.isRunning.collect { _running.value = it }
        }
    }

    fun start(durationMinutes: Int) {
        // ⭐ THIS IS THE MAIN FIX ⭐
        // Determine if we are starting a brand new session or resuming a paused one.
        val isResuming = !_running.value && _remaining.value > 0

        // Only enable DND and start tracking on a completely new session.
        // Your DND logic is preserved and called correctly.
        if (!isResuming) {
            dndViewModel.enableDND()
            FocusSessionManager.startTracking(getApplication())
        }

        val intent = Intent(getApplication(), FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            // Only add the duration if it's a new session.
            // If it's a resume, the service will use its own remaining time.
            if (!isResuming) {
                putExtra(FocusTimerService.EXTRA_DURATION, durationMinutes * 60 * 1000L)
            }
        }

        startService(intent)
    }

    fun pause() {
        // Your existing pause logic is correct and preserved.
        dndViewModel.disableDND()
        val intent = Intent(getApplication(), FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_PAUSE
        }
        startService(intent)
    }

    fun stop() {
        // Your existing stop logic is correct and preserved.
        dndViewModel.disableDND()
        FocusSessionManager.stopTracking(getApplication())

        val intent = Intent(getApplication(), FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        }
        startService(intent)
    }

    private fun startService(intent: Intent) {
        val app = getApplication<Application>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
    }
}
