package com.example.floworbit.data.violationlog



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "violation_logs")
data class ViolationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val label: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMillis: Long = 0L, // optional
    val note: String? = null
)
