package com.example.floworbit.data.blockedapps

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {

    @Query("SELECT * FROM blocked_apps")
    fun getAll(): Flow<List<BlockedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: BlockedApp)

    @Delete
    suspend fun delete(app: BlockedApp)

    @Query("DELETE FROM blocked_apps")
    suspend fun clearAll()

    // ⭐ NEW: THIS IS REQUIRED FOR ACCESSIBILITY BLOCKER
    @Query("SELECT packageName FROM blocked_apps")
    suspend fun getBlockedPackageNames(): List<String>

    // ⭐ REPLACE THE OLD isBlocked FUNCTION WITH THIS ONE
    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName)")
    suspend fun isBlocked(packageName: String): Boolean


}
