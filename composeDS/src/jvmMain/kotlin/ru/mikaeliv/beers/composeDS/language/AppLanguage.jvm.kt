package ru.mikaeliv.beers.composeDS.language

import java.util.Locale

actual fun applyAppLanguage(languageTag: String) {
    Locale.setDefault(Locale.forLanguageTag(languageTag))
}
