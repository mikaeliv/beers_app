package ru.mikaeliv.beers.composeDS.language

import android.os.Build
import android.os.LocaleList
import java.util.Locale

actual fun applyAppLanguage(languageTag: String) {
    val locale = Locale.forLanguageTag(languageTag)
    Locale.setDefault(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.setDefault(LocaleList(locale))
    }
}
