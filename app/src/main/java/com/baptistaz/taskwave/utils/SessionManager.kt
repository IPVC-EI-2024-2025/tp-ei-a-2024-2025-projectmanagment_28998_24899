package com.baptistaz.taskwave.utils

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "taskwave_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_AUTH_ID = "auth_id"
    private const val KEY_USER_ID      = "user_id"

    fun saveAccessToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveAuthId(context: Context, id: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_AUTH_ID, id).apply()
    }

    fun getAuthId(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AUTH_ID, null)
    }

    fun saveUserId(ctx: Context, id: String) =                       // ← NOVO
        ctx.prefs().edit().putString(KEY_USER_ID, id).apply()

    fun getUserId(ctx: Context): String? =                           // ← NOVO
        ctx.prefs().getString(KEY_USER_ID, null)

    fun clearAccessToken(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    private fun Context.prefs() =
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
