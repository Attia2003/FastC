package com.example.fastaf.ui.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope

object PrefsUtils {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER = "username"
    private const val KEY_ROLE = "user_role"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserToPrefs(context: Context, username: String) {
        getPrefs(context).edit().putString(KEY_USER, username).apply()
    }

    fun saveUserRole(context: Context, role: String) {
        getPrefs(context).edit().putString(KEY_ROLE, role).apply()
    }

    fun getUserFromPrefs(context: Context): String =
        getPrefs(context).getString(KEY_USER, "") ?: ""

    fun getUserRole(context: Context): String =
        getPrefs(context).getString(KEY_ROLE, "") ?: ""

    fun clearPrefs(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}