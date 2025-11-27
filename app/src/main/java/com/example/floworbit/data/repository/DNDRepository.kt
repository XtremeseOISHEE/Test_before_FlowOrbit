package com.example.floworbit.data.repository

import android.content.Context
import com.example.floworbit.util.DNDManager

class DNDRepository(private val context: Context) {

    private val dndManager = DNDManager(context)

    // Correct method name from DNDManager
    fun hasDNDAccess(): Boolean = dndManager.hasAccess()

    fun enableDND() {
        if (hasDNDAccess()) dndManager.enableDND()
    }

    fun disableDND() {
        if (hasDNDAccess()) dndManager.disableDND()
    }
}
