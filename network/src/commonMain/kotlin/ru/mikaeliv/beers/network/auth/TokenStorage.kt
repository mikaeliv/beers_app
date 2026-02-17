package ru.mikaeliv.beers.network.auth

/**
 * Хранилище токенов авторизации.
 */
expect class TokenStorage() {
    /**
     * Сохраняет access token.
     */
    fun saveAccessToken(token: String)

    /**
     * Получает access token.
     */
    fun getAccessToken(): String?

    /**
     * Сохраняет refresh token.
     */
    fun saveRefreshToken(token: String)

    /**
     * Получает refresh token.
     */
    fun getRefreshToken(): String?

    /**
     * Сохраняет email пользователя.
     */
    fun saveEmail(email: String)

    /**
     * Получает email пользователя.
     */
    fun getEmail(): String?

    /**
     * Очищает все данные (при логауте).
     */
    fun clear()

    /**
     * Проверяет, авторизован ли пользователь.
     */
    fun isLoggedIn(): Boolean
}
