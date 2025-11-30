package com.example.floworbit.domain.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val isBlocked: Boolean = false
)
