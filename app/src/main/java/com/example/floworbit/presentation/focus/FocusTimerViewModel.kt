package com.example.floworbit.presentation.focus

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.service.FocusTimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusTimerViewModel(app: Application) : AndroidViewModel(app) {

    private val _remaining = MutableStateFlow(0L)
    val remaining = _remaining.asStateFlow()

    private val _running = MutableStateFlow(false)
    val running = _running.asStateFlow()

    init {
        // ✅ Use viewModelScope to collect service flows
        viewModelScope.launch {
            FocusTimerService.remainingTime.collect { _remaining.value = it }
        }

        viewModelScope.launch {
            FocusTimerService.isRunning.collect { _running.value = it }
        }
    }

    fun start(durationMinutes: Int) {
        val intent = Intent(getApplication(), FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION, durationMinutes * 60 * 1000L)
        }

        val app = getApplication<Application>()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Android 8+
            app.startForegroundService(intent)
        } else {
            // Android 7 and below
            app.startService(intent)
        }
    }


    fun pause() {
        val intent = Intent(getApplication(), FocusTimerService::class.java)
        intent.action = FocusTimerService.ACTION_PAUSE
        getApplication<Application>().startService(intent)
    }

    fun stop() {
        val intent = Intent(getApplication(), FocusTimerService::class.java)
        intent.action = FocusTimerService.ACTION_STOP
        getApplication<Application>().startService(intent)
    }
}
