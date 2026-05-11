package ru.mikaeliv.beers.network.auth

/**
 * Хранилище токенов авторизации.
 */
interface ITokenStorage {
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

/**
 * Платформенное хранилище токенов.
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

/**
 * Адаптер над платформенным TokenStorage.
 * Позволяет подменять хранилище в тестах без доступа к реальным Preferences/localStorage.
 */
class DefaultTokenStorage(
    private val delegate: TokenStorage = TokenStorage(),
) : ITokenStorage {
    override fun saveAccessToken(token: String) {
        delegate.saveAccessToken(token)
    }

    override fun getAccessToken(): String? =
        delegate.getAccessToken()

    override fun saveRefreshToken(token: String) {
        delegate.saveRefreshToken(token)
    }

    override fun getRefreshToken(): String? =
        delegate.getRefreshToken()

    override fun saveEmail(email: String) {
        delegate.saveEmail(email)
    }

    override fun getEmail(): String? =
        delegate.getEmail()

    override fun clear() {
        delegate.clear()
    }

    override fun isLoggedIn(): Boolean =
        delegate.isLoggedIn()
}
