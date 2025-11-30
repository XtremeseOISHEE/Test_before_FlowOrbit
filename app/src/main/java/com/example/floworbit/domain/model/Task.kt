package com.example.floworbit.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks") // <-- Add this annotation
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // <-- Add this annotation
    val title: String,
    val description: String = "",
    val deadlineMillis: Long? = null,
    val priority: Int = 0,
    val category: String? = null,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis() // <-- Add this field
)
