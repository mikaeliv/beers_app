package ru.mikaeliv.beers.network.auth

import platform.Foundation.NSUserDefaults

private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_EMAIL = "email"

actual class TokenStorage actual constructor() {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun saveAccessToken(token: String) {
        defaults.setObject(token, KEY_ACCESS_TOKEN)
    }

    actual fun getAccessToken(): String? =
        defaults.stringForKey(KEY_ACCESS_TOKEN)

    actual fun saveRefreshToken(token: String) {
        defaults.setObject(token, KEY_REFRESH_TOKEN)
    }

    actual fun getRefreshToken(): String? =
        defaults.stringForKey(KEY_REFRESH_TOKEN)

    actual fun saveEmail(email: String) {
        defaults.setObject(email, KEY_EMAIL)
    }

    actual fun getEmail(): String? =
        defaults.stringForKey(KEY_EMAIL)

    actual fun clear() {
        defaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        defaults.removeObjectForKey(KEY_REFRESH_TOKEN)
        defaults.removeObjectForKey(KEY_EMAIL)
    }

    actual fun isLoggedIn(): Boolean =
        getAccessToken() != null
}
