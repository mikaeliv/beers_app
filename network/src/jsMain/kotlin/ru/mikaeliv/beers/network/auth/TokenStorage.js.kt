package ru.mikaeliv.beers.network.auth

import kotlinx.browser.localStorage

private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_EMAIL = "email"

actual class TokenStorage actual constructor() {
    actual fun saveAccessToken(token: String) {
        localStorage.setItem(KEY_ACCESS_TOKEN, token)
    }

    actual fun getAccessToken(): String? =
        localStorage.getItem(KEY_ACCESS_TOKEN)

    actual fun saveRefreshToken(token: String) {
        localStorage.setItem(KEY_REFRESH_TOKEN, token)
    }

    actual fun getRefreshToken(): String? =
        localStorage.getItem(KEY_REFRESH_TOKEN)

    actual fun saveEmail(email: String) {
        localStorage.setItem(KEY_EMAIL, email)
    }

    actual fun getEmail(): String? =
        localStorage.getItem(KEY_EMAIL)

    actual fun clear() {
        localStorage.removeItem(KEY_ACCESS_TOKEN)
        localStorage.removeItem(KEY_REFRESH_TOKEN)
        localStorage.removeItem(KEY_EMAIL)
    }

    actual fun isLoggedIn(): Boolean =
        getAccessToken() != null
}
