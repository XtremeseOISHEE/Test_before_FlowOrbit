package com.example.floworbit.data.violationlog



import android.content.Context
import kotlinx.coroutines.flow.Flow

class ViolationLogRepository(context: Context) {
    private val dao = ViolationLogDatabase.getInstance(context).violationLogDao()

    suspend fun addLog(log: ViolationLog) = dao.insert(log)
    fun allLogs(): Flow<List<ViolationLog>> = dao.getAll()

    suspend fun insert(log: ViolationLog) {
        dao.insert(log)
    }
}
