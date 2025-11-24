package com.example.floworbit.domain.usecase


import com.example.floworbit.data.repository.FocusSessionRepository

class StartFocusUseCase(private val repo: FocusSessionRepository) {
    fun startSessionPlaceholder() {
        // For now nothing special — repository is used by ViewModel directly.
        // Kept for clean architecture expansion.
    }
}


