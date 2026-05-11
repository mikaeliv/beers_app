package ru.mikaeliv.beers.feature.profile

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import ru.mikaeliv.beers.network.ApiResult
import ru.mikaeliv.beers.network.api.IAuthApi
import ru.mikaeliv.beers.network.dto.AuthResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileComponentTest {

    /**
     * Проверяет, что email из конструктора попадает в state.
     */
    @Test
    fun initialStateContainsEmail() {
        val fixture = createFixture(email = "user@example.com")

        assertEquals("user@example.com", fixture.component.state.value.email)
    }

    /**
     * Проверяет, что back и settings клики пробрасываются наружу.
     */
    @Test
    fun navigationClicksCallOutput() {
        val fixture = createFixture()

        fixture.component.onBack()
        fixture.component.onSettingsClick()

        assertEquals(1, fixture.output.backCalls)
        assertEquals(1, fixture.output.openSettingsCalls)
    }

    /**
     * Проверяет logout: AuthApi очищается, затем наружу отправляется событие logout.
     */
    @Test
    fun onLogoutCallsAuthApiAndOutput() {
        val fixture = createFixture()

        fixture.component.onLogout()

        assertEquals(1, fixture.authApi.logoutCalls)
        assertEquals(1, fixture.output.logoutCalls)
    }

    private fun createFixture(email: String = "user@example.com"): Fixture {
        val authApi = FakeAuthApi()
        val output = FakeOutput()
        val component = DefaultProfileComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            email = email,
            authApi = authApi,
            output = output
        )
        return Fixture(component, authApi, output)
    }

    private data class Fixture(
        val component: ProfileComponent,
        val authApi: FakeAuthApi,
        val output: FakeOutput,
    )

    private class FakeAuthApi : IAuthApi {
        var logoutCalls = 0
        override suspend fun register(email: String, password: String): ApiResult<AuthResponse> = ApiResult.Error("Unused")
        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> = ApiResult.Error("Unused")
        override suspend fun refresh(): ApiResult<AuthResponse> = ApiResult.Error("Unused")
        override fun logout() { logoutCalls += 1 }
        override fun isLoggedIn(): Boolean = false
    }

    private class FakeOutput : ProfileComponent.Output {
        var backCalls = 0
        var openSettingsCalls = 0
        var logoutCalls = 0
        override fun back() { backCalls += 1 }
        override fun openSettings() { openSettingsCalls += 1 }
        override fun onLogout() { logoutCalls += 1 }
    }
}
