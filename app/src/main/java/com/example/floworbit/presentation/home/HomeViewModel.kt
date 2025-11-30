package com.example.floworbit.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.data.task.TaskRepository // <-- Import the real one
import com.example.floworbit.domain.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 1. Change ViewModel to AndroidViewModel to get the Application context
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // 2. Initialize the REAL repository
    private val repo: TaskRepository = TaskRepository(application)

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

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repo.updateTask(task)
        }
    }

    // This function can no longer be synchronous because it queries the database.
    // The UI will get the task from the `tasks` flow instead.
    // You can remove this function or modify it to be suspend.
    // fun getTaskById(id: String): Task? = repo.getTaskById(id) // This line will now error
}
