package com.example.floworbit.data.repository



import com.example.floworbit.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InMemoryTaskRepository {
    private val _tasks = MutableStateFlow<List<Task>>(sampleTasks())
    val tasks: StateFlow<List<Task>> = _tasks

    fun getTaskById(id: String): Task? = _tasks.value.firstOrNull { it.id == id }

    fun addTask(task: Task) {
        _tasks.update { current -> current + task }
    }

    fun updateTask(updated: Task) {
        _tasks.update { current ->
            current.map { if (it.id == updated.id) updated else it }
        }
    }

    fun deleteTask(id: String) {
        _tasks.update { current -> current.filterNot { it.id == id } }
    }

    private fun sampleTasks(): List<Task> = listOf(
        Task(title = "Read chapter 1", description = "Intro to algorithms", priority = 1, category = "Study"),
        Task(title = "Write lab report", description = "FlowOrbit proposal", priority = 2, category = "Work"),
        Task(title = "Quick walk", description = "Take a 15 min walk", priority = 0, category = "Health")
    )
}
