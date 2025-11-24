package com.example.floworbit.presentation.focus



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.data.repository.FocusSessionRepository
import com.example.floworbit.domain.model.FocusSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusTimerViewModel(
    private val repo: FocusSessionRepository = FocusSessionRepository()
) : ViewModel() {

    // UI state
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis: StateFlow<Long> = _remainingMillis.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    val durationMillis: StateFlow<Long> = _durationMillis.asStateFlow()

    // session logs
    val sessions = repo.sessions

    // internal bookkeeping
    private var tickJob: Job? = null
    private var sessionStartTime: Long = 0L
    private var pausedAtMillis: Long? = null
    private var interruptedDuringSession: Boolean = false

    /**
     * Start a fresh focus session with minutes
     */
    fun start(minutes: Int) {
        val ms = minutes * 60_000L
        startMillis(ms)
    }

    /**
     * Start with explicit millis (used by presets)
     */
    private fun startMillis(ms: Long) {
        if (ms <= 0L) return
        tickJob?.cancel()
        _durationMillis.value = ms
        _remainingMillis.value = ms
        _isRunning.value = true
        interruptedDuringSession = false
        sessionStartTime = System.currentTimeMillis()
        pausedAtMillis = null
        startTickLoop()
    }

    private fun startTickLoop() {
        tickJob = viewModelScope.launch {
            while (_isRunning.value && _remainingMillis.value > 0L) {
                delay(1000L)
                _remainingMillis.value = (_remainingMillis.value - 1000L).coerceAtLeast(0L)
                if (_remainingMillis.value == 0L) {
                    completeSession()
                }
            }
        }
    }

    fun pause() {
        if (!_isRunning.value) return
        tickJob?.cancel()
        _isRunning.value = false
        pausedAtMillis = _remainingMillis.value
    }

    fun resume() {
        if (_isRunning.value) return
        val remaining = pausedAtMillis ?: _remainingMillis.value
        _remainingMillis.value = remaining
        _isRunning.value = true
        pausedAtMillis = null
        startTickLoop()
    }

    fun reset() {
        tickJob?.cancel()
        _isRunning.value = false
        _remainingMillis.value = 0L
        _durationMillis.value = 0L
        pausedAtMillis = null
        interruptedDuringSession = false
    }

    /**
     * Force stop by user (considered an interruption)
     */
    fun forceStop() {
        tickJob?.cancel()
        _isRunning.value = false
        val now = System.currentTimeMillis()
        val elapsed = if (sessionStartTime > 0L) now - sessionStartTime else _durationMillis.value - _remainingMillis.value
        val session = FocusSession(
            startTimeMillis = sessionStartTime.takeIf { it > 0L } ?: now - elapsed,
            endTimeMillis = now,
            durationMillis = elapsed,
            interrupted = true
        )
        repo.addSession(session)
        // reset internal
        _remainingMillis.value = 0L
        _durationMillis.value = 0L
        sessionStartTime = 0L
        interruptedDuringSession = true
    }

    /**
     * Called when session reaches zero
     */
    private fun completeSession() {
        _isRunning.value = false
        val now = System.currentTimeMillis()
        val elapsed = _durationMillis.value
        val session = FocusSession(
            startTimeMillis = sessionStartTime.takeIf { it > 0L } ?: now - elapsed,
            endTimeMillis = now,
            durationMillis = elapsed,
            interrupted = interruptedDuringSession
        )
        repo.addSession(session)
        // reset internal but keep last duration for UI if needed
        _remainingMillis.value = 0L
        _durationMillis.value = 0L
        sessionStartTime = 0L
    }

    /**
     * Helper to format remaining millis to mm:ss (UI can call this)
     */
    fun formatMillisToClock(ms: Long): String {
        val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
