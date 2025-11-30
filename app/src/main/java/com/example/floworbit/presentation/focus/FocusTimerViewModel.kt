package com.example.floworbit.presentation.focus

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.presentation.dnd.DNDViewModel
import com.example.floworbit.service.FocusTimerService
import com.example.floworbit.presentation.focus.FocusSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        // 🔥 TURN ON DND
        dndViewModel.enableDND()

        // ✅ Start tracking app usage
        FocusSessionManager.startTracking(getApplication())

        val intent = Intent(getApplication(), FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION, durationMinutes * 60 * 1000L)
        }

        val app = getApplication<Application>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
    }

    fun pause() {
        // 🔥 OPTIONAL: Turn off DND when paused
        dndViewModel.disableDND()

        // ✅ Optional: stop tracking on pause if desired
        // FocusSessionManager.stopTracking(getApplication())

        val intent = Intent(getApplication(), FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_PAUSE
        }
        getApplication<Application>().startService(intent)
    }

    fun stop() {
        // 🔥 TURN OFF DND
        dndViewModel.disableDND()

        // ✅ Stop tracking app usage
        FocusSessionManager.stopTracking(getApplication())

        val intent = Intent(getApplication(), FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }


}
