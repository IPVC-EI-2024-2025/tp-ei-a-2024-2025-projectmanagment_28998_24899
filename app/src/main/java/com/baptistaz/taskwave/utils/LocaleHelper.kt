package com.baptistaz.taskwave.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Used to switch language dynamically (EN ↔ PT).
 */
object LocaleHelper {

    /**
     * Updates the application's context to use the specified language.
     *
     * @param context Current application or activity context.
     * @param language Language ("en", "pt").
     * @return Updated context with the new locale.
     */
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
