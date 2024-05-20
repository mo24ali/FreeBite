package com.example.freebite2.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SharedPreferencesUtil {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    fun isUserLoggedIn(context: Context): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false)
        Log.d("SharedPreferencesUtil", "isUserLoggedIn: $isLoggedIn")
        return isLoggedIn
    }

    fun setUserLoggedIn(context: Context, loggedIn: Boolean) {
        val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
        Log.d("SharedPreferencesUtil", "setUserLoggedIn: $loggedIn")
    }
}
