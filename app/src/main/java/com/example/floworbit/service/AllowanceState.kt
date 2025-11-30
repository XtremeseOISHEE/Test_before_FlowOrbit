package com.example.floworbit.service

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A simple, shared object to track if a temporary allowance is active for a SPECIFIC app.
 */
object AllowanceState {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    // Now holds the package name of the allowed app, or null if none.
    private val _allowedPackage = MutableStateFlow<String?>(null)
    val allowedPackage = _allowedPackage.asStateFlow()

    /**
     * Starts a 5-minute allowance timer for a specific app.
     * During this time, `allowedPackage` will hold its package name.
     */
    fun startAllowance(packageName: String) {
        timerJob?.cancel() // Cancel any existing timer
        _allowedPackage.value = packageName
        timerJob = scope.launch {
            delay(5 * 60 * 1000L) // 5 minutes
            // Only reset if the timer for this specific package is still the active one
            if (isActive && _allowedPackage.value == packageName) {
                _allowedPackage.value = null
            }
        }
    }

    /**
     * Immediately stops any active allowance timer and resets the state.
     */
    fun stopAllowance() {
        timerJob?.cancel()
        _allowedPackage.value = null
    }
}
