package com.example.floworbit.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class AppBlockEntity(
    @PrimaryKey val packageName: String
)
