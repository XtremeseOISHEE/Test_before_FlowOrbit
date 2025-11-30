package com.example.floworbit.data.local



import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppBlockDao {


    @Query("SELECT * FROM blocked_apps")
    suspend fun getBlockedApps(): List<AppBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockApp(app: AppBlockEntity)

    @Delete
    suspend fun unblockApp(app: AppBlockEntity)


}
