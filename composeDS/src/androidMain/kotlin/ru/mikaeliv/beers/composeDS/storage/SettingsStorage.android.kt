package ru.mikaeliv.beers.composeDS.storage

import android.content.Context
import android.content.SharedPreferences

actual class SettingsStorage actual constructor(context: Any?) {
    private val prefs: SharedPreferences = (context as Context).getSharedPreferences(
        "beers_settings",
        Context.MODE_PRIVATE
    )

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
