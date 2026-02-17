package ru.mikaeliv.beers.composeDS.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.mikaeliv.beers.composeDS.storage.SettingsStorage

/**
 * Глобальное состояние темы приложения с персистентным хранением.
 */
object ThemeState {
    private var storage: SettingsStorage? = null

    var isDarkTheme by mutableStateOf(false)
        private set

    /**
     * Инициализирует состояние темы из хранилища.
     * Должен быть вызван при старте приложения.
     */
    fun init(settingsStorage: SettingsStorage) {
        storage = settingsStorage
        isDarkTheme = settingsStorage.getBoolean(DARK_THEME, false)
    }

    /**
     * Устанавливает тему и сохраняет в хранилище.
     */
    fun toggleDarkTheme(enabled: Boolean) {
        isDarkTheme = enabled
        storage?.putBoolean(DARK_THEME, enabled)
    }

    private const val DARK_THEME = "dark_theme"
}

/**
 * CompositionLocal для доступа к состоянию темы из любого места.
 */
val LocalIsDarkTheme = compositionLocalOf { false }
