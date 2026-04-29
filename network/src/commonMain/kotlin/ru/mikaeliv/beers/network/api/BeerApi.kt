package ru.mikaeliv.beers.network.api

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.mikaeliv.beers.network.ApiResult
import ru.mikaeliv.beers.network.AuthenticatedApiClient
import ru.mikaeliv.beers.network.dto.BeerRequest
import ru.mikaeliv.beers.network.dto.BeerResponse
import ru.mikaeliv.beers.network.dto.BeersPageResponse

/**
 * API для работы с пивом.
 * Все запросы требуют авторизации.
 */
class BeerApi(
    private val apiClient: AuthenticatedApiClient = AuthenticatedApiClient(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Получает список пив с пагинацией.
     *
     * @param page номер страницы (0-based)
     * @param size размер страницы
     * @return результат с пагинированным ответом (content, page) или ошибка
     */
    suspend fun getBeers(page: Int = 0, size: Int = 20): ApiResult<BeersPageResponse> =
        apiClient.get("/beers?page=$page&size=$size")

    /**
     * Добавляет новое пиво.
     *
     * @param request запрос с данными пива
     * @return результат с созданным пивом или ошибка
     */
    suspend fun saveBeer(
        request: BeerRequest,
        imageBytes: ByteArray? = null,
        imageFileName: String = "beer-image.jpg",
        imageContentType: String = "image/jpeg",
    ): ApiResult<BeerResponse> {
        if (request.id == null && imageBytes == null) {
            return ApiResult.Error("Image is required")
        }

        return apiClient.postMultipart(
            endpoint = "/beers",
            body = MultiPartFormDataContent(
                formData {
                    append(
                        key = "beer",
                        value = json.encodeToString(request),
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        }
                    )
                    if (imageBytes != null) {
                        append(
                            key = "image",
                            value = imageBytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, imageContentType)
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "form-data; name=\"image\"; filename=\"$imageFileName\""
                                )
                            }
                        )
                    }
                }
            )
        )
    }

    suspend fun addBeer(request: BeerRequest, imageBytes: ByteArray): ApiResult<BeerResponse> =
        saveBeer(request = request.copy(id = null), imageBytes = imageBytes)

    suspend fun updateBeer(request: BeerRequest, imageBytes: ByteArray? = null): ApiResult<BeerResponse> =
        saveBeer(request = request, imageBytes = imageBytes)

    suspend fun getBeerImage(imageUrl: String): ApiResult<ByteArray> =
        apiClient.get(imageUrl)

    /**
     * Удаляет пиво по ID.
     *
     * @param id идентификатор пива
     * @return результат успеха или ошибка
     */
    suspend fun deleteBeer(id: String): ApiResult<Unit> =
        apiClient.deleteUnit("/beers/$id")
}
