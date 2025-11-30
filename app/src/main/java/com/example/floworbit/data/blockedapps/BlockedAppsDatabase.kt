package com.example.floworbit.data.blockedapps


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BlockedApp::class], version = 1, exportSchema = false)
abstract class BlockedAppsDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao

    companion object {
        @Volatile private var INSTANCE: BlockedAppsDatabase? = null
        fun getInstance(context: Context): BlockedAppsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BlockedAppsDatabase::class.java,
                    "blocked_apps_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
