package ru.mikaeliv.beers.network.auth

import java.util.prefs.Preferences

private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_EMAIL = "email"

actual class TokenStorage actual constructor() {
    private val prefs: Preferences = Preferences.userNodeForPackage(TokenStorage::class.java)

    actual fun saveAccessToken(token: String) {
        prefs.put(KEY_ACCESS_TOKEN, token)
        prefs.flush()
    }

    actual fun getAccessToken(): String? =
        prefs.get(KEY_ACCESS_TOKEN, null)

    actual fun saveRefreshToken(token: String) {
        prefs.put(KEY_REFRESH_TOKEN, token)
        prefs.flush()
    }

    actual fun getRefreshToken(): String? =
        prefs.get(KEY_REFRESH_TOKEN, null)

    actual fun saveEmail(email: String) {
        prefs.put(KEY_EMAIL, email)
        prefs.flush()
    }

    actual fun getEmail(): String? =
        prefs.get(KEY_EMAIL, null)

    actual fun clear() {
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.remove(KEY_EMAIL)
        prefs.flush()
    }

    actual fun isLoggedIn(): Boolean =
        getAccessToken() != null
}
