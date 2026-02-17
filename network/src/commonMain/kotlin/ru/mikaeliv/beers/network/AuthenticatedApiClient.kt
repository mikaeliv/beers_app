package ru.mikaeliv.beers.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ru.mikaeliv.beers.network.auth.TokenStorage
import ru.mikaeliv.beers.network.dto.AuthResponse
import ru.mikaeliv.beers.network.dto.RefreshRequest

/**
 * API клиент с автоматической авторизацией.
 * Добавляет Authorization: Bearer header ко всем запросам.
 * Автоматически обновляет токены при получении 401.
 */
class AuthenticatedApiClient(
    @PublishedApi internal val httpClient: HttpClient = createHttpClient(),
    @PublishedApi internal val tokenStorage: TokenStorage = TokenStorage(),
) {
    // Флаг для предотвращения бесконечного цикла обновления токенов
    @PublishedApi
    internal var isRefreshing = false

    /**
     * Выполняет GET-запрос с авторизацией.
     */
    suspend inline fun <reified T> get(endpoint: String): ApiResult<T> =
        executeWithRetry { token ->
            httpClient.get(buildUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

    /**
     * Выполняет POST-запрос с авторизацией.
     */
    suspend inline fun <reified T, reified R> post(endpoint: String, body: T): ApiResult<R> =
        executeWithRetry { token ->
            httpClient.post(buildUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

    /**
     * Выполняет PUT-запрос с авторизацией.
     */
    suspend inline fun <reified T, reified R> put(endpoint: String, body: T): ApiResult<R> =
        executeWithRetry { token ->
            httpClient.put(buildUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

    /**
     * Выполняет DELETE-запрос с авторизацией.
     */
    suspend inline fun <reified T> delete(endpoint: String): ApiResult<T> =
        executeWithRetry { token ->
            httpClient.delete(buildUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

    /**
     * Выполняет DELETE-запрос с авторизацией и возвращает Unit.
     */
    suspend fun deleteUnit(endpoint: String): ApiResult<Unit> =
        executeWithRetryUnit { token ->
            httpClient.delete(buildUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

    /**
     * Выполняет запрос с автоматическим обновлением токена при 401.
     */
    @PublishedApi
    internal suspend inline fun <reified T> executeWithRetry(
        crossinline request: suspend (token: String) -> HttpResponse,
    ): ApiResult<T> = try {
        val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("No access token", 401)
        val response = request(token)

        when {
            response.status.isSuccess() -> ApiResult.Success(response.body())
            response.status == HttpStatusCode.Unauthorized && !isRefreshing -> {
                // Токен протух — пробуем обновить
                val refreshResult = refreshTokens()
                if (refreshResult) {
                    // Повторяем запрос с новым токеном
                    val newToken = tokenStorage.getAccessToken()
                        ?: return ApiResult.Error("No access token after refresh", 401)
                    val retryResponse = request(newToken)
                    if (retryResponse.status.isSuccess()) {
                        ApiResult.Success(retryResponse.body())
                    } else {
                        ApiResult.Error(
                            message = "HTTP ${retryResponse.status.value}: ${retryResponse.status.description}",
                            code = retryResponse.status.value
                        )
                    }
                } else {
                    // Refresh не удался — нужна повторная авторизация
                    ApiResult.Error("Session expired", 401)
                }
            }
            else -> ApiResult.Error(
                message = "HTTP ${response.status.value}: ${response.status.description}",
                code = response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(
            message = e.message ?: "Unknown error",
            cause = e
        )
    }

    /**
     * Выполняет запрос с автоматическим обновлением токена при 401 (для Unit ответов).
     */
    @PublishedApi
    internal suspend fun executeWithRetryUnit(
        request: suspend (token: String) -> HttpResponse,
    ): ApiResult<Unit> = try {
        val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("No access token", 401)
        val response = request(token)

        when {
            response.status.isSuccess() -> ApiResult.Success(Unit)
            response.status == HttpStatusCode.Unauthorized && !isRefreshing -> {
                val refreshResult = refreshTokens()
                if (refreshResult) {
                    val newToken = tokenStorage.getAccessToken()
                        ?: return ApiResult.Error("No access token after refresh", 401)
                    val retryResponse = request(newToken)
                    if (retryResponse.status.isSuccess()) {
                        ApiResult.Success(Unit)
                    } else {
                        ApiResult.Error(
                            message = "HTTP ${retryResponse.status.value}: ${retryResponse.status.description}",
                            code = retryResponse.status.value
                        )
                    }
                } else {
                    ApiResult.Error("Session expired", 401)
                }
            }
            else -> ApiResult.Error(
                message = "HTTP ${response.status.value}: ${response.status.description}",
                code = response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(
            message = e.message ?: "Unknown error",
            cause = e
        )
    }

    /**
     * Обновляет токены через /auth/refresh.
     * @return true если обновление успешно, false если нужна повторная авторизация
     */
    @PublishedApi
    internal suspend fun refreshTokens(): Boolean {
        val refreshToken = tokenStorage.getRefreshToken() ?: return false

        isRefreshing = true
        return try {
            val response = httpClient.post(buildUrl("/auth/refresh")) {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }

            if (response.status.isSuccess()) {
                val authResponse: AuthResponse = response.body()
                tokenStorage.saveAccessToken(authResponse.accessToken)
                tokenStorage.saveRefreshToken(authResponse.refreshToken)
                true
            } else {
                // Refresh token тоже протух — очищаем токены
                tokenStorage.clear()
                false
            }
        } catch (e: Exception) {
            tokenStorage.clear()
            false
        } finally {
            isRefreshing = false
        }
    }

    @PublishedApi
    internal fun buildUrl(endpoint: String): String {
        val base = ApiConfig.baseUrl.trimEnd('/')
        val path = endpoint.trimStart('/')
        return "$base/$path"
    }

    fun close() {
        httpClient.close()
    }
}
