package com.baptistaz.taskwave.utils

import android.content.Context

/**
 * Manages session-related data in SharedPreferences.
 * Stores access token, user identifiers, and language preference.
 */
object SessionManager {

    private const val PREF_NAME = "taskwave_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_AUTH_ID = "auth_id"
    private const val KEY_USER_ID = "user_id"

    /** Saves the Supabase access token (JWT) to shared preferences. */
    fun saveAccessToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    /** Stored access token. */
    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_TOKEN, null)
    }

    /** Saves the Supabase auth ID. */
    fun saveAuthId(context: Context, id: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_AUTH_ID, id).apply()
    }

    /** Supabase auth ID. */
    fun getAuthId(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AUTH_ID, null)
    }

    /** Saves the internal user UUID (from the 'utilizador' table). */
    fun saveUserId(ctx: Context, id: String) {
        ctx.prefs().edit().putString(KEY_USER_ID, id).apply()
    }

    /** Internal user UUID. */
    fun getUserId(ctx: Context): String? {
        return ctx.prefs().getString(KEY_USER_ID, null)
    }

    /** Clears all stored session data. */
    fun clearAccessToken(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    /** Current language code ("pt" by default). */
    fun getLanguage(context: Context): String {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("language", "pt") ?: "pt"
    }

    /** Sets the application's language ("en", "pt"). */
    fun setLanguage(context: Context, langCode: String) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("language", langCode)
            .apply()
    }

    /** Convenience extension to access app preferences. */
    private fun Context.prefs() =
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
