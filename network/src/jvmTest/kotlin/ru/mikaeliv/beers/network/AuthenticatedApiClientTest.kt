package ru.mikaeliv.beers.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import ru.mikaeliv.beers.network.auth.ITokenStorage
import ru.mikaeliv.beers.network.dto.AuthResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AuthenticatedApiClientTest {
    private lateinit var tokenStorage: InMemoryTokenStorage

    /**
     * Перед каждым тестом создает чистое in-memory хранилище токенов.
     */
    @BeforeTest
    fun setUp() {
        tokenStorage = InMemoryTokenStorage()
    }

    /**
     * Проверяет, что авторизованный запрос без access token завершается ошибкой без HTTP-вызова.
     */
    @Test
    fun getReturnsErrorWhenAccessTokenIsMissing() = runTest {
        var requestCount = 0
        val client = AuthenticatedApiClient(
            httpClient = mockHttpClient {
                requestCount++
                respondJson("""{}""")
            },
            tokenStorage = tokenStorage
        )

        val result = client.get<AuthResponse>("/protected")

        val error = assertIs<ApiResult.Error>(result)
        assertEquals(401, error.code)
        assertEquals("No access token", error.message)
        assertEquals(0, requestCount)
    }

    /**
     * Проверяет, что клиент добавляет Authorization header с текущим access token.
     */
    @Test
    fun getSendsBearerAuthorizationHeader() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        tokenStorage.saveAccessToken("access-token")
        val client = AuthenticatedApiClient(
            httpClient = mockHttpClient { request ->
                requests += request
                respondJson("""{"accessToken":"ok","refreshToken":"ok"}""")
            },
            tokenStorage = tokenStorage
        )

        val result = client.get<AuthResponse>("/protected")

        assertIs<ApiResult.Success<AuthResponse>>(result)
        assertEquals("Bearer access-token", requests.single().headers[HttpHeaders.Authorization])
    }

    /**
     * Проверяет, что при 401 клиент обновляет токены и повторяет исходный запрос с новым access token.
     */
    @Test
    fun getRefreshesTokensAndRetriesRequestAfterUnauthorizedResponse() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        tokenStorage.saveAccessToken("old-access")
        tokenStorage.saveRefreshToken("old-refresh")
        val client = AuthenticatedApiClient(
            httpClient = mockHttpClient { request ->
                requests += request
                when (request.url.encodedPath) {
                    "/protected" -> {
                        val authHeader = request.headers[HttpHeaders.Authorization]
                        if (authHeader == "Bearer old-access") {
                            respondError(HttpStatusCode.Unauthorized)
                        } else {
                            respondJson("""{"accessToken":"resource-access","refreshToken":"resource-refresh"}""")
                        }
                    }
                    "/auth/refresh" -> respondJson("""{"accessToken":"new-access","refreshToken":"new-refresh"}""")
                    else -> respondError(HttpStatusCode.NotFound)
                }
            },
            tokenStorage = tokenStorage
        )

        val result = client.get<AuthResponse>("/protected")

        val success = assertIs<ApiResult.Success<AuthResponse>>(result)
        assertEquals("resource-access", success.data.accessToken)
        assertEquals("new-access", tokenStorage.getAccessToken())
        assertEquals("new-refresh", tokenStorage.getRefreshToken())
        assertEquals(
            listOf("Bearer old-access", null, "Bearer new-access"),
            requests.map { it.headers[HttpHeaders.Authorization] }
        )
    }

    /**
     * Проверяет, что при неудачном refresh клиент очищает токены и возвращает ошибку сессии.
     */
    @Test
    fun getClearsTokensAndReturnsSessionExpiredWhenRefreshFails() = runTest {
        tokenStorage.saveAccessToken("old-access")
        tokenStorage.saveRefreshToken("old-refresh")
        val client = AuthenticatedApiClient(
            httpClient = mockHttpClient { request ->
                when (request.url.encodedPath) {
                    "/protected" -> respondError(HttpStatusCode.Unauthorized)
                    "/auth/refresh" -> respondError(HttpStatusCode.Unauthorized)
                    else -> respondError(HttpStatusCode.NotFound)
                }
            },
            tokenStorage = tokenStorage
        )

        val result = client.get<AuthResponse>("/protected")

        val error = assertIs<ApiResult.Error>(result)
        assertEquals(401, error.code)
        assertEquals("Session expired", error.message)
        assertNull(tokenStorage.getAccessToken())
        assertNull(tokenStorage.getRefreshToken())
    }

    private fun mockHttpClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient =
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

    private class InMemoryTokenStorage : ITokenStorage {
        private var accessToken: String? = null
        private var refreshToken: String? = null
        private var email: String? = null

        override fun saveAccessToken(token: String) {
            accessToken = token
        }

        override fun getAccessToken(): String? = accessToken

        override fun saveRefreshToken(token: String) {
            refreshToken = token
        }

        override fun getRefreshToken(): String? = refreshToken

        override fun saveEmail(email: String) {
            this.email = email
        }

        override fun getEmail(): String? = email

        override fun clear() {
            accessToken = null
            refreshToken = null
            email = null
        }

        override fun isLoggedIn(): Boolean = accessToken != null
    }
}
