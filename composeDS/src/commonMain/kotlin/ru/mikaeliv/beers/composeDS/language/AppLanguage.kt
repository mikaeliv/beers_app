package ru.mikaeliv.beers.composeDS.language

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.mikaeliv.beers.composeDS.storage.SettingsStorage

enum class AppLanguage(val tag: String) {
    English("en"),
    Russian("ru");

    companion object {
        fun fromTag(tag: String): AppLanguage = entries.firstOrNull { it.tag == tag } ?: English
    }
}

object LanguageState {
    private var storage: SettingsStorage? = null

    var language by mutableStateOf(AppLanguage.English)
        private set

    fun init(settingsStorage: SettingsStorage) {
        storage = settingsStorage
        setLanguage(
            language = AppLanguage.fromTag(settingsStorage.getString(LANGUAGE, AppLanguage.English.tag)),
            persist = false
        )
    }

    fun selectLanguage(language: AppLanguage) {
        setLanguage(language = language, persist = true)
    }

    private fun setLanguage(language: AppLanguage, persist: Boolean) {
        this.language = language
        applyAppLanguage(language.tag)
        if (persist) {
            storage?.putString(LANGUAGE, language.tag)
        }
    }

    private const val LANGUAGE = "language"
}

expect fun applyAppLanguage(languageTag: String)
