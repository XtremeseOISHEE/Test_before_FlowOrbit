package com.example.floworbit.presentation.dnd



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.data.repository.DNDRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DNDViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DNDRepository(application)

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission

    init {
        checkPermission()
    }

    fun checkPermission() {
        viewModelScope.launch {
            _hasPermission.value = repository.hasDNDAccess()
        }
    }

    fun enableDND() {
        repository.enableDND()
    }

    fun disableDND() {
        repository.disableDND()
    }
}
