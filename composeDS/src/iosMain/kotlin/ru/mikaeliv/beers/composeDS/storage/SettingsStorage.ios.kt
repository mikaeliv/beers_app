package ru.mikaeliv.beers.composeDS.storage

import platform.Foundation.NSUserDefaults

actual class SettingsStorage actual constructor(context: Any?) {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (defaults.objectForKey(key) != null) {
            defaults.boolForKey(key)
        } else {
            defaultValue
        }
    }

    actual fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, key)
        defaults.synchronize()
    }

    actual fun getString(key: String, defaultValue: String): String {
        return defaults.stringForKey(key) ?: defaultValue
    }

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, key)
        defaults.synchronize()
    }
}
