package com.example.floworbit.presentation.blockedapps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floworbit.data.blockedapps.BlockedApp
import com.example.floworbit.data.blockedapps.BlockedAppsRepository
import com.example.floworbit.domain.model.InstalledApp
import com.example.floworbit.util.AppUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockedAppsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BlockedAppsRepository(application)

    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps

    private val ownPackage = application.packageName

    init {
        viewModelScope.launch {
            val installedApps = AppUtils.getInstalledApps(getApplication())

            repo.getBlockedApps().collectLatest { blockedList ->
                val blockedSet = blockedList.map { it.packageName }.toSet()
                _apps.value = installedApps.map { app ->
                    val isLocked = app.packageName == ownPackage // own app is always locked
                    app.copy(isBlocked = app.packageName in blockedSet || isLocked)
                }
            }
        }
    }

    fun toggleBlock(app: InstalledApp) {
        viewModelScope.launch {
            if (app.isBlocked) {
                repo.removeBlockedApp(BlockedApp(app.packageName, app.appName))
            } else {
                repo.addBlockedApp(BlockedApp(app.packageName, app.appName))
            }
        }
    }
}
