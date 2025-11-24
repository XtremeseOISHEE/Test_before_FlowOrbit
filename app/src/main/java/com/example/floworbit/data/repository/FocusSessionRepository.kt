package com.example.floworbit.data.repository



import com.example.floworbit.domain.model.FocusSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FocusSessionRepository {

    private val _sessions = MutableStateFlow<List<FocusSession>>(emptyList())
    val sessions: StateFlow<List<FocusSession>> = _sessions

    fun addSession(session: FocusSession) {
        _sessions.update { it + session }
    }

    fun clearSessions() {
        _sessions.value = emptyList()
    }
}
