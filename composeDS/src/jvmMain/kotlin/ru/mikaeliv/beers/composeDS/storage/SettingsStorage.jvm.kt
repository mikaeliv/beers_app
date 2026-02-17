package ru.mikaeliv.beers.composeDS.storage

import java.util.prefs.Preferences

actual class SettingsStorage actual constructor(context: Any?) {
    private val prefs: Preferences = Preferences.userRoot().node("ru.mikaeliv.beers")

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
        prefs.flush()
    }
}
