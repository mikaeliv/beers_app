package ru.mikaeliv.beers.feature.profile

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ru.mikaeliv.beers.core.Beer
import ru.mikaeliv.beers.core.SyncStatus
import ru.mikaeliv.beers.data.IBeerRepository
import ru.mikaeliv.beers.network.ApiResult
import ru.mikaeliv.beers.network.api.IAuthApi
import ru.mikaeliv.beers.network.dto.AuthResponse
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileComponentTest {

    /**
     * Проверяет, что email из конструктора попадает в state.
     */
    @Test
    fun initialStateContainsEmail() = runTest {
        val fixture = createFixture(email = "user@example.com")

        assertEquals("user@example.com", fixture.component.state.value.email)
    }

    /**
     * Проверяет, что back и settings клики пробрасываются наружу.
     */
    @Test
    fun navigationClicksCallOutput() = runTest {
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
    fun onLogoutCallsAuthApiAndOutput() = runTest {
        val fixture = createFixture()

        fixture.component.onLogout()

        assertEquals(1, fixture.authApi.logoutCalls)
        assertEquals(1, fixture.output.logoutCalls)
    }

    /**
     * Проверяет, что статистика профиля пересчитывается из списка пива аккаунта.
     */
    @Test
    fun collectsAccountStatsFromRepository() = runTest {
        val fixture = createFixture()

        fixture.repository.emit(
            listOf(
                beer(id = 1L, rating = 4),
                beer(id = 2L, rating = 5),
                beer(id = 3L, rating = 3),
            )
        )
        advanceUntilIdle()

        assertEquals(3, fixture.component.state.value.beerCount)
        assertEquals(4.0, fixture.component.state.value.averageRating)
    }

    private fun TestScope.createFixture(email: String = "user@example.com"): Fixture {
        val authApi = FakeAuthApi()
        val repository = FakeBeerRepository()
        val output = FakeOutput()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val component = DefaultProfileComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            email = email,
            repo = repository,
            authApi = authApi,
            output = output,
            scope = CoroutineScope(dispatcher)
        )
        return Fixture(component, repository, authApi, output)
    }

    private data class Fixture(
        val component: DefaultProfileComponent,
        val repository: FakeBeerRepository,
        val authApi: FakeAuthApi,
        val output: FakeOutput,
    )

    private class FakeBeerRepository : IBeerRepository {
        private val beers = MutableStateFlow<List<Beer>>(emptyList())
        fun emit(value: List<Beer>) { beers.value = value }
        override fun getAll(): Flow<List<Beer>> = beers
        override suspend fun getById(id: Long): Beer? = null
        override suspend fun getByServerId(serverId: String): Beer? = null
        override suspend fun add(beer: Beer): Long = 0L
        override suspend fun insertFromServer(serverId: String, name: String, abv: Double, rating: Int, comment: String?, photoBytes: ByteArray?): Long = 0L
        override suspend fun update(beer: Beer) = Unit
        override suspend fun delete(id: Long) = Unit
        override suspend fun getPendingCreate(): List<Beer> = emptyList()
        override suspend fun getPendingDelete(): List<Beer> = emptyList()
        override suspend fun updateSyncStatus(id: Long, serverId: String?, status: SyncStatus) = Unit
        override suspend fun deletePhysically(id: Long) = Unit
        override suspend fun deleteSynced() = Unit
        override suspend fun deleteAll() = Unit
    }

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

    private fun beer(id: Long, rating: Int) = Beer(
        id = id,
        name = "Beer $id",
        abv = 5.0,
        comment = null,
        rating = rating,
    )
}
