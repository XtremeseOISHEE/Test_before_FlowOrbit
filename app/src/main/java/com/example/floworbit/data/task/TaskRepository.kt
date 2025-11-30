package com.example.floworbit.data.task



import android.content.Context
import com.example.floworbit.domain.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(context: Context) {

    private val dao = TaskDatabase.getInstance(context).taskDao()

    val tasks: Flow<List<Task>> = dao.getTasks()

    suspend fun addTask(task: Task) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(id: String) {
        dao.deleteTask(id)
    }

    suspend fun getTaskById(id: String): Task? {
        return dao.getTaskById(id)
    }
}
