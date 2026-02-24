package ru.mikaeliv.beers.network.api

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
     * @param name название пива
     * @param rating оценка (1-5)
     * @param abv крепость (%)
     * @param description описание (опционально)
     * @return результат с созданным пивом или ошибка
     */
    suspend fun addBeer(
        name: String,
        rating: Int,
        abv: Double,
        description: String? = null,
    ): ApiResult<BeerResponse> = apiClient.post(
        endpoint = "/beers",
        body = BeerRequest(
            id = null,
            name = name,
            rating = rating,
            abv = abv,
            description = description
        )
    )

    /**
     * Добавляет новое пиво.
     *
     * @param request запрос с данными пива
     * @return результат с созданным пивом или ошибка
     */
    suspend fun addBeer(request: BeerRequest): ApiResult<BeerResponse> =
        apiClient.post("/beers", request)

    /**
     * Удаляет пиво по ID.
     *
     * @param id идентификатор пива
     * @return результат успеха или ошибка
     */
    suspend fun deleteBeer(id: String): ApiResult<Unit> =
        apiClient.deleteUnit("/beers/$id")
}
