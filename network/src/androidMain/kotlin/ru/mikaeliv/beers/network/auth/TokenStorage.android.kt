package ru.mikaeliv.beers.network.auth

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "beers_auth"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_EMAIL = "email"

private var appContext: Context? = null

/**
 * Инициализация TokenStorage для Android.
 * Вызвать в Application.onCreate() или MainActivity.onCreate().
 */
fun initTokenStorage(context: Context) {
    appContext = context.applicationContext
}

actual class TokenStorage actual constructor() {
    private val prefs: SharedPreferences
        get() = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?: throw IllegalStateException("TokenStorage not initialized. Call initTokenStorage(context) first.")

    actual fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    actual fun getAccessToken(): String? =
        prefs.getString(KEY_ACCESS_TOKEN, null)

    actual fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    actual fun getRefreshToken(): String? =
        prefs.getString(KEY_REFRESH_TOKEN, null)

    actual fun saveEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    actual fun getEmail(): String? =
        prefs.getString(KEY_EMAIL, null)

    actual fun clear() {
        prefs.edit().clear().apply()
    }

    actual fun isLoggedIn(): Boolean =
        getAccessToken() != null
}
