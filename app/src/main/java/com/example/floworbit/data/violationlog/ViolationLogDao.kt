package com.example.floworbit.data.violationlog



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ViolationLogDao {
    @Insert
    suspend fun insert(log: ViolationLog)

    @Query("SELECT * FROM violation_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ViolationLog>>
}
