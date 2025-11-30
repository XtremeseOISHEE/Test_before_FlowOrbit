package com.example.floworbit.presentation.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.data.violationlog.ViolationLogRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val logRepo = ViolationLogRepository(application)

    private val _violationData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val violationData: StateFlow<Map<String, Int>> = _violationData.asStateFlow()

    init {
        loadViolationData()
    }

    private fun loadViolationData() {
        viewModelScope.launch {
            logRepo.allLogs().collect { logs ->
                val groupedData = logs
                    // ⭐ FIX: Filter out any logs where the label might be null or blank
                    .filter { !it.label.isNullOrBlank() }
                    // Now, the compiler knows 'it.label' can't be null in the next steps
                    .groupBy { it.label!! } // Use '!!' because we know it's safe after the filter
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)
                    .toMap()

                _violationData.value = groupedData
            }
        }
    }
}
