package com.example.floworbit.data.violationlog



import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ViolationLog::class], version = 1, exportSchema = false)
abstract class ViolationLogDatabase : RoomDatabase() {
    abstract fun violationLogDao(): ViolationLogDao

    companion object {
        @Volatile private var INSTANCE: ViolationLogDatabase? = null
        fun getInstance(context: Context): ViolationLogDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ViolationLogDatabase::class.java,
                    "violation_log_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
