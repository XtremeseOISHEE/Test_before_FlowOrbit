package com.example.floworbit.domain.model



data class FocusSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMillis: Long,
    val interrupted: Boolean = false
)

