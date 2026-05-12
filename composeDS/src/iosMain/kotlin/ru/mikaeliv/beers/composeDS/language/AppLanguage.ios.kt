package ru.mikaeliv.beers.composeDS.language

import platform.Foundation.NSArray
import platform.Foundation.NSUserDefaults

actual fun applyAppLanguage(languageTag: String) {
    NSUserDefaults.standardUserDefaults.setObject(NSArray.arrayWithObject(languageTag), "AppleLanguages")
    NSUserDefaults.standardUserDefaults.synchronize()
}
