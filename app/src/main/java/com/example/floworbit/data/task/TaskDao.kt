package com.example.floworbit.data.task

import androidx.room.*
import com.example.floworbit.domain.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // ⭐ THIS IS THE ONLY LINE THAT CHANGES ⭐
    @Query("SELECT * FROM tasks ORDER BY priority DESC, createdAt DESC")
    fun getTasks(): Flow<List<Task>>

    // All your other functions are perfect and remain unchanged
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)
}
