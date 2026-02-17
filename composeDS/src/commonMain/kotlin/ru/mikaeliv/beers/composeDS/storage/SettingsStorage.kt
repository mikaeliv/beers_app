package ru.mikaeliv.beers.composeDS.storage

/**
 * Хранилище настроек приложения.
 * @param context контекст платформы (Context на Android, null на других платформах)
 */
expect class SettingsStorage(context: Any?) {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
}
