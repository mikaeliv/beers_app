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
}
