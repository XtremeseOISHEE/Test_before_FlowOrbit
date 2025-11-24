package com.example.floworbit.presentation.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.data.repository.InMemoryTaskRepository
import com.example.floworbit.domain.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: InMemoryTaskRepository = InMemoryTaskRepository()
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repo.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addTask(title: String, description: String, category: String?) {
        viewModelScope.launch {
            repo.addTask(Task(title = title, description = description, category = category))
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repo.deleteTask(id)
        }
    }

    // Add these inside HomeViewModel
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repo.updateTask(task)
        }
    }

    fun getTaskById(id: String): Task? = repo.getTaskById(id)

}
