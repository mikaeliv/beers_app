package ru.mikaeliv.beers.composeDS.storage

import kotlinx.browser.localStorage

actual class SettingsStorage actual constructor(context: Any?) {
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return localStorage.getItem(key)?.toBooleanStrictOrNull() ?: defaultValue
    }

    actual fun putBoolean(key: String, value: Boolean) {
        localStorage.setItem(key, value.toString())
    }

    actual fun getString(key: String, defaultValue: String): String {
        return localStorage.getItem(key) ?: defaultValue
    }

    actual fun putString(key: String, value: String) {
        localStorage.setItem(key, value)
    }
}
