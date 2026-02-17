package ru.mikaeliv.beers.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Базовый API клиент для выполнения HTTP-запросов.
 */
class ApiClient(
    @PublishedApi internal val httpClient: HttpClient = createHttpClient(),
) {
    /**
     * Выполняет GET-запрос.
     *
     * @param endpoint путь эндпоинта (без базового URL)
     * @return результат запроса
     */
    suspend inline fun <reified T> get(endpoint: String): ApiResult<T> =
        safeRequest {
            httpClient.get(buildUrl(endpoint))
        }

    /**
     * Выполняет POST-запрос.
     *
     * @param endpoint путь эндпоинта (без базового URL)
     * @param body тело запроса
     * @return результат запроса
     */
    suspend inline fun <reified T, reified R> post(endpoint: String, body: T?): ApiResult<R> =
        safeRequest {
            httpClient.post(buildUrl(endpoint)) {
                contentType(ContentType.Application.Json)
                body?.let {
                    setBody(it)
                }
            }
        }

    /**
     * Выполняет PUT-запрос.
     *
     * @param endpoint путь эндпоинта (без базового URL)
     * @param body тело запроса
     * @return результат запроса
     */
    suspend inline fun <reified T, reified R> put(endpoint: String, body: T): ApiResult<R> =
        safeRequest {
            httpClient.put(buildUrl(endpoint)) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

    /**
     * Выполняет PATCH-запрос.
     *
     * @param endpoint путь эндпоинта (без базового URL)
     * @param body тело запроса
     * @return результат запроса
     */
    suspend inline fun <reified T, reified R> patch(endpoint: String, body: T): ApiResult<R> =
        safeRequest {
            httpClient.patch(buildUrl(endpoint)) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

    /**
     * Выполняет DELETE-запрос.
     *
     * @param endpoint путь эндпоинта (без базового URL)
     * @return результат запроса
     */
    suspend inline fun <reified T> delete(endpoint: String): ApiResult<T> =
        safeRequest {
            httpClient.delete(buildUrl(endpoint))
        }

    /**
     * Выполняет DELETE-запрос и возвращает Unit при успехе.
     *
     * @param endpoint путь эндпоинта (без базового URL)
     * @return результат запроса
     */
    suspend fun deleteUnit(endpoint: String): ApiResult<Unit> =
        safeRequestUnit {
            httpClient.delete(buildUrl(endpoint))
        }

    /**
     * Безопасно выполняет запрос с обработкой ошибок.
     */
    suspend inline fun <reified T> safeRequest(
        request: () -> HttpResponse,
    ): ApiResult<T> = try {
        val response = request()
        if (response.status.isSuccess()) {
            ApiResult.Success(response.body())
        } else {
            ApiResult.Error(
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
     * Безопасно выполняет запрос без парсинга тела ответа.
     */
    suspend fun safeRequestUnit(
        request: suspend () -> HttpResponse,
    ): ApiResult<Unit> = try {
        val response = request()
        if (response.status.isSuccess()) {
            ApiResult.Success(Unit)
        } else {
            ApiResult.Error(
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
     * Формирует полный URL.
     */
    fun buildUrl(endpoint: String): String {
        val base = ApiConfig.baseUrl.trimEnd('/')
        val path = endpoint.trimStart('/')
        return "$base/$path"
    }

    /**
     * Закрывает HTTP клиент.
     */
    fun close() {
        httpClient.close()
    }
}
