package com.example.floworbit.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object AllowanceManager {

    private const val PREF = "floworbit_allowances"
    private const val KEY_PREFIX = "allow_until_"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    /** Save allowance until given time */
    fun setAllowanceUntil(context: Context, packageName: String, untilMillis: Long) {
        Log.d("ALLOW", "Setting allowance for $packageName until $untilMillis")
        prefs(context)
            .edit()
            .putLong(KEY_PREFIX + packageName, untilMillis)
            .apply()
    }

    /** Check if allowance is active */
    fun isAllowedNow(context: Context, packageName: String): Boolean {
        val until = prefs(context).getLong(KEY_PREFIX + packageName, 0L)
        val now = System.currentTimeMillis()
        val allowed = now <= until

        Log.d("ALLOW", "Check $packageName => now=$now until=$until result=$allowed")

        return allowed
    }

    /** Remove allowance */
    fun clearAllowance(context: Context, packageName: String) {
        Log.d("ALLOW", "Clearing allowance for $packageName")
        prefs(context)
            .edit()
            .remove(KEY_PREFIX + packageName)
            .apply()
    }
}
