package com.example.floworbit.data.blockedapps

import android.content.Context
import kotlinx.coroutines.flow.Flow

class BlockedAppsRepository(context: Context) {

    private val dao = BlockedAppsDatabase.getInstance(context).blockedAppDao()

    fun getBlockedApps(): Flow<List<BlockedApp>> = dao.getAll()

    suspend fun addBlockedApp(app: BlockedApp) = dao.insert(app)

    suspend fun removeBlockedApp(app: BlockedApp) = dao.delete(app)

    suspend fun clearAll() = dao.clearAll()

    // ⭐ NEW: REQUIRED FOR AppBlockerService
    suspend fun getBlockedPackageNames(): List<String> {
        return dao.getBlockedPackageNames()
    }
    suspend fun isAppBlocked(packageName: String): Boolean {
        return dao.isBlocked(packageName)
    }

}
