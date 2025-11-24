package com.example.floworbit.domain.model



import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val deadlineMillis: Long? = null,
    val priority: Int = 0,
    val category: String? = null,
    val completed: Boolean = false
)
