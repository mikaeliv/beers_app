package ru.mikaeliv.beers.composeDS.language

actual fun applyAppLanguage(languageTag: String) {
    setPreferredLanguage(languageTag)
}

@Suppress("UnsafeCastFromDynamic")
private fun setPreferredLanguage(languageTag: String) {
    js(
        """
        Object.defineProperty(window.navigator, 'languages', {
            get: function() { return [languageTag]; },
            configurable: true
        });
        Object.defineProperty(window.navigator, 'language', {
            get: function() { return languageTag; },
            configurable: true
        });
        """
    )
}
