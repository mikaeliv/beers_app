package ru.mikaeliv.beers.feature.auth

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ru.mikaeliv.beers.network.ApiResult
import ru.mikaeliv.beers.network.api.IAuthApi
import ru.mikaeliv.beers.network.dto.AuthResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthComponentTest {

    /**
     * Проверяет начальное состояние формы авторизации.
     */
    @Test
    fun initialStateIsLoginModeAndEmpty() = runTest {
        val fixture = createFixture()

        val state = fixture.component.state.value

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertTrue(state.isLoginMode)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    /**
     * Проверяет, что ввод email и password обновляет state и очищает прошлую ошибку.
     */
    @Test
    fun inputChangesUpdateStateAndClearError() = runTest {
        val fixture = createFixture()
        fixture.component.onSubmit()

        fixture.component.onEmailChange("user@example.com")
        fixture.component.onPasswordChange("secret")

        val state = fixture.component.state.value
        assertEquals("user@example.com", state.email)
        assertEquals("secret", state.password)
        assertNull(state.error)
    }

    /**
     * Проверяет переключение между входом и регистрацией.
     */
    @Test
    fun switchModeTogglesLoginModeAndClearsError() = runTest {
        val fixture = createFixture()
        fixture.component.onSubmit()

        fixture.component.switchMode()

        assertFalse(fixture.component.state.value.isLoginMode)
        assertNull(fixture.component.state.value.error)
    }

    /**
     * Проверяет, что пустая форма не отправляется в API и показывает ошибку.
     */
    @Test
    fun submitWithBlankFieldsShowsValidationError() = runTest {
        val fixture = createFixture()

        fixture.component.onSubmit()
        advanceUntilIdle()

        assertEquals("Заполните все поля", fixture.component.state.value.error)
        assertEquals(0, fixture.authApi.loginCalls)
        assertEquals(0, fixture.output.successCalls)
    }

    /**
     * Проверяет успешный login: вызывается API входа и наружу отправляется onAuthSuccess.
     */
    @Test
    fun submitInLoginModeCallsLoginAndOutputSuccess() = runTest {
        val fixture = createFixture()
        fixture.component.onEmailChange("user@example.com")
        fixture.component.onPasswordChange("secret")

        fixture.component.onSubmit()
        advanceUntilIdle()

        assertEquals(1, fixture.authApi.loginCalls)
        assertEquals(0, fixture.authApi.registerCalls)
        assertEquals(1, fixture.output.successCalls)
    }

    /**
     * Проверяет успешную регистрацию: в режиме регистрации вызывается register.
     */
    @Test
    fun submitInRegisterModeCallsRegisterAndOutputSuccess() = runTest {
        val fixture = createFixture()
        fixture.component.switchMode()
        fixture.component.onEmailChange("user@example.com")
        fixture.component.onPasswordChange("secret")

        fixture.component.onSubmit()
        advanceUntilIdle()

        assertEquals(0, fixture.authApi.loginCalls)
        assertEquals(1, fixture.authApi.registerCalls)
        assertEquals(1, fixture.output.successCalls)
    }

    /**
     * Проверяет ошибку API: loading выключается, а message попадает в state.error.
     */
    @Test
    fun submitShowsApiError() = runTest {
        val fixture = createFixture(result = ApiResult.Error("Bad credentials", 401))
        fixture.component.onEmailChange("user@example.com")
        fixture.component.onPasswordChange("wrong")

        fixture.component.onSubmit()
        advanceUntilIdle()

        assertFalse(fixture.component.state.value.isLoading)
        assertEquals("Bad credentials", fixture.component.state.value.error)
        assertEquals(0, fixture.output.successCalls)
    }

    /**
     * Проверяет, что onDestroy отменяет незавершенный submit и не пропускает onAuthSuccess после отмены.
     */
    @Test
    fun onDestroyCancelsPendingSubmit() = runTest {
        val pendingResult = CompletableDeferred<ApiResult<AuthResponse>>()
        val fixture = createFixture(authApi = FakeAuthApi { pendingResult.await() })
        fixture.component.onEmailChange("user@example.com")
        fixture.component.onPasswordChange("secret")

        fixture.component.onSubmit()
        advanceUntilIdle()
        assertTrue(fixture.component.state.value.isLoading)
        assertEquals(1, fixture.authApi.loginCalls)

        fixture.component.onDestroy()
        pendingResult.complete(ApiResult.Success(AuthResponse("access", "refresh")))
        advanceUntilIdle()

        assertEquals(0, fixture.output.successCalls)
    }

    private fun TestScope.createFixture(
        result: ApiResult<AuthResponse> = ApiResult.Success(AuthResponse("access", "refresh")),
        authApi: FakeAuthApi = FakeAuthApi(result),
    ): Fixture {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val output = FakeOutput()
        val component = DefaultAuthComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            authApi = authApi,
            output = output,
            scope = CoroutineScope(dispatcher),
            workDispatcher = dispatcher
        )
        return Fixture(component, authApi, output)
    }

    private data class Fixture(
        val component: AuthComponent,
        val authApi: FakeAuthApi,
        val output: FakeOutput,
    )

    private class FakeAuthApi(
        private val resultProvider: suspend () -> ApiResult<AuthResponse>,
    ) : IAuthApi {
        constructor(result: ApiResult<AuthResponse>) : this({ result })

        var loginCalls = 0
        var registerCalls = 0
        var logoutCalls = 0

        override suspend fun register(email: String, password: String): ApiResult<AuthResponse> {
            registerCalls += 1
            return resultProvider()
        }

        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
            loginCalls += 1
            return resultProvider()
        }

        override suspend fun refresh(): ApiResult<AuthResponse> = resultProvider()

        override fun logout() {
            logoutCalls += 1
        }

        override fun isLoggedIn(): Boolean = false
    }

    private class FakeOutput : AuthComponent.Output {
        var successCalls = 0

        override fun onAuthSuccess() {
            successCalls += 1
        }
    }
}
