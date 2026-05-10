package ru.mikaeliv.beers.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import ru.mikaeliv.beers.network.dto.AuthResponse
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ApiClientTest {

    /**
     * Проверяет, что клиент корректно склеивает базовый URL и endpoint с лишними слешами.
     */
    @Test
    fun buildUrlTrimsExtraSlashes() {
        val client = ApiClient(mockHttpClient { respondJson("""{}""") })

        assertEquals("http://localhost:8085/auth/login", client.buildUrl("/auth/login"))
    }

    /**
     * Проверяет, что успешный HTTP-ответ парсится в ApiResult.Success с нужным DTO.
     */
    @Test
    fun getReturnsSuccessForSuccessfulResponse() = runTest {
        val client = ApiClient(
            mockHttpClient {
                respondJson("""{"accessToken":"access-token","refreshToken":"refresh-token"}""")
            }
        )

        val result = client.get<AuthResponse>("/auth/me")

        val success = assertIs<ApiResult.Success<AuthResponse>>(result)
        assertEquals("access-token", success.data.accessToken)
        assertEquals("refresh-token", success.data.refreshToken)
    }

    /**
     * Проверяет, что неуспешный HTTP-статус превращается в ApiResult.Error с кодом ответа.
     */
    @Test
    fun getReturnsErrorForHttpErrorResponse() = runTest {
        val client = ApiClient(
            mockHttpClient {
                respondError(HttpStatusCode.NotFound)
            }
        )

        val result = client.get<AuthResponse>("/missing")

        val error = assertIs<ApiResult.Error>(result)
        assertEquals(404, error.code)
        assertEquals("HTTP 404: Not Found", error.message)
    }

    /**
     * Проверяет, что исключение во время запроса не падает наружу, а упаковывается в ApiResult.Error.
     */
    @Test
    fun getReturnsErrorForRequestException() = runTest {
        val client = ApiClient(
            mockHttpClient {
                throw IOException("Network is unavailable")
            }
        )

        val result = client.get<AuthResponse>("/auth/me")

        val error = assertIs<ApiResult.Error>(result)
        assertEquals("Network is unavailable", error.message)
        assertNotNull(error.cause)
    }

    private fun mockHttpClient(handler: MockRequestHandler): HttpClient =
        HttpClient(MockEngine) {
            engine { addHandler(handler) }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

    private fun MockRequestHandleScope.respondJson(json: String): HttpResponseData = respond(
        content = json,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )
}
