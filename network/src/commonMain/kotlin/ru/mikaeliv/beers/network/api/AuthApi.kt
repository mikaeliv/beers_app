package ru.mikaeliv.beers.network.api

import ru.mikaeliv.beers.network.ApiClient
import ru.mikaeliv.beers.network.ApiResult
import ru.mikaeliv.beers.network.auth.TokenStorage
import ru.mikaeliv.beers.network.dto.AuthRequest
import ru.mikaeliv.beers.network.dto.AuthResponse
import ru.mikaeliv.beers.network.dto.RefreshRequest

/**
 * API для авторизации.
 */
class AuthApi(
    private val apiClient: ApiClient = ApiClient(),
    private val tokenStorage: TokenStorage = TokenStorage(),
) {
    /**
     * Регистрация нового пользователя.
     *
     * @param email email пользователя
     * @param password пароль
     * @return результат с токенами или ошибка
     */
    suspend fun register(email: String, password: String): ApiResult<AuthResponse> {
        val result = apiClient.post<AuthRequest, AuthResponse>(
            endpoint = "/auth/register",
            body = AuthRequest(email, password)
        )
        result.onSuccess { response ->
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            tokenStorage.saveEmail(email)
        }
        return result
    }

    /**
     * Авторизация пользователя.
     *
     * @param email email пользователя
     * @param password пароль
     * @return результат с токенами или ошибка
     */
    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        val result = apiClient.post<AuthRequest, AuthResponse>(
            endpoint = "/auth/login",
            body = AuthRequest(email, password)
        )
        result.onSuccess { response ->
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            tokenStorage.saveEmail(email)
        }
        return result
    }

    /**
     * Обновление токенов.
     *
     * @return результат с новыми токенами или ошибка
     */
    suspend fun refresh(): ApiResult<AuthResponse> {
        val refreshToken = tokenStorage.getRefreshToken()
            ?: return ApiResult.Error("No refresh token available")

        val result = apiClient.post<RefreshRequest, AuthResponse>(
            endpoint = "/auth/refresh",
            body = RefreshRequest(refreshToken)
        )
        result.onSuccess { response ->
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
        }
        return result
    }

    /**
     * Выход из аккаунта (очистка токенов).
     */
    fun logout() {
        tokenStorage.clear()
    }

    /**
     * Проверяет, авторизован ли пользователь.
     */
    fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn()
}
