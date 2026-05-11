package ru.mikaeliv.beers.ui

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import ru.mikaeliv.beers.core.Beer
import ru.mikaeliv.beers.core.SyncActions
import ru.mikaeliv.beers.core.SyncStatus
import ru.mikaeliv.beers.data.IBeerRepository
import ru.mikaeliv.beers.network.ApiResult
import ru.mikaeliv.beers.network.api.IAuthApi
import ru.mikaeliv.beers.network.auth.ITokenStorage
import ru.mikaeliv.beers.network.dto.AuthResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RootComponentTest {

    /**
     * Сбрасывает тестовый Main dispatcher после каждого сценария навигации.
     */
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Проверяет, что без access token приложение стартует с экрана авторизации.
     */
    @Test
    fun startsFromAuthWhenUserIsNotLoggedIn() = runTest {
        val fixture = createFixture(tokenStorage = InMemoryTokenStorage())

        assertIs<RootComponent.Child.Auth>(fixture.root.activeChild())
    }

    /**
     * Проверяет, что с access token приложение стартует со списка и запускает первичный pull.
     */
    @Test
    fun startsFromListWhenUserIsLoggedIn() = runTest {
        val fixture = createFixture(tokenStorage = loggedInStorage())
        advanceUntilIdle()

        assertIs<RootComponent.Child.List>(fixture.root.activeChild())
        assertEquals(1, fixture.syncActions.pullOnlyCalls)
    }

    /**
     * Проверяет переходы из списка в add/detail/profile и возврат назад из дочерних экранов.
     */
    @Test
    fun listNavigationPushesAddDetailAndProfile() = runTest {
        val fixture = createFixture(tokenStorage = loggedInStorage())
        val list = assertIs<RootComponent.Child.List>(fixture.root.activeChild())

        list.component.onAddClick()
        val add = assertIs<RootComponent.Child.Add>(fixture.root.activeChild())
        add.component.onBack()
        assertIs<RootComponent.Child.List>(fixture.root.activeChild())

        val listAfterAdd = assertIs<RootComponent.Child.List>(fixture.root.activeChild())
        listAfterAdd.component.onOpen(42L)
        val detail = assertIs<RootComponent.Child.Detail>(fixture.root.activeChild())
        detail.component.onBack()
        assertIs<RootComponent.Child.List>(fixture.root.activeChild())

        val listAfterDetail = assertIs<RootComponent.Child.List>(fixture.root.activeChild())
        listAfterDetail.component.onProfileClick()
        val profile = assertIs<RootComponent.Child.Profile>(fixture.root.activeChild())
        assertEquals("user@example.com", profile.component.state.value.email)
    }

    /**
     * Проверяет, что профиль открывает настройки, back возвращает профиль, а logout заменяет стек на Auth.
     */
    @Test
    fun profileNavigationOpensSettingsAndLogoutReturnsToAuth() = runTest {
        val fixture = createFixture(tokenStorage = loggedInStorage())
        val list = assertIs<RootComponent.Child.List>(fixture.root.activeChild())
        list.component.onProfileClick()
        val profile = assertIs<RootComponent.Child.Profile>(fixture.root.activeChild())

        profile.component.onSettingsClick()
        val settings = assertIs<RootComponent.Child.Settings>(fixture.root.activeChild())
        settings.component.onBack()
        val profileAfterBack = assertIs<RootComponent.Child.Profile>(fixture.root.activeChild())

        profileAfterBack.component.onLogout()

        assertIs<RootComponent.Child.Auth>(fixture.root.activeChild())
        assertEquals(1, fixture.authApi.logoutCalls)
    }

    /**
     * Проверяет, что успешная авторизация заменяет экран Auth на List.
     */
    @Test
    fun authSuccessReplacesStackWithList() = runTest {
        val fixture = createFixture(tokenStorage = InMemoryTokenStorage())
        val auth = assertIs<RootComponent.Child.Auth>(fixture.root.activeChild())
        auth.component.onEmailChange("user@example.com")
        auth.component.onPasswordChange("secret")

        auth.component.onSubmit()
        advanceUntilActive<RootComponent.Child.List>(fixture.root)

        assertEquals(1, fixture.authApi.loginCalls)
        assertIs<RootComponent.Child.List>(fixture.root.activeChild())
    }

    private fun TestScope.createFixture(
        tokenStorage: InMemoryTokenStorage = loggedInStorage(),
        repository: FakeBeerRepository = FakeBeerRepository(),
        authApi: FakeAuthApi = FakeAuthApi(),
        syncActions: FakeSyncActions = FakeSyncActions(),
    ): Fixture {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val root = DefaultRootComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            repo = repository,
            authApi = authApi,
            tokenStorage = tokenStorage,
            syncEngine = syncActions
        )
        return Fixture(root, repository, authApi, tokenStorage, syncActions)
    }

    private suspend inline fun <reified T : RootComponent.Child> TestScope.advanceUntilActive(root: RootComponent) {
        repeat(20) {
            advanceUntilIdle()
            if (root.activeChild() is T) return
            yield()
        }
        advanceUntilIdle()
        assertTrue(root.activeChild() is T)
    }

    private fun RootComponent.activeChild(): RootComponent.Child =
        stack.value.active.instance

    private fun loggedInStorage() = InMemoryTokenStorage(
        accessToken = "access-token",
        email = "user@example.com"
    )

    private data class Fixture(
        val root: RootComponent,
        val repository: FakeBeerRepository,
        val authApi: FakeAuthApi,
        val tokenStorage: InMemoryTokenStorage,
        val syncActions: FakeSyncActions,
    )

    private class FakeBeerRepository : IBeerRepository {
        private val beers = MutableStateFlow<List<Beer>>(emptyList())
        private val beersById = mutableMapOf(42L to beer(42L))

        override fun getAll(): Flow<List<Beer>> = beers
        override suspend fun getById(id: Long): Beer? = beersById[id]
        override suspend fun getByServerId(serverId: String): Beer? = null
        override suspend fun add(beer: Beer): Long = beer.id ?: 1L
        override suspend fun insertFromServer(
            serverId: String,
            name: String,
            abv: Double,
            rating: Int,
            comment: String?,
            photoBytes: ByteArray?,
        ): Long = 1L
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
        var loginCalls = 0
        var registerCalls = 0
        var logoutCalls = 0

        override suspend fun register(email: String, password: String): ApiResult<AuthResponse> {
            registerCalls += 1
            return ApiResult.Success(AuthResponse("access", "refresh"))
        }

        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
            loginCalls += 1
            return ApiResult.Success(AuthResponse("access", "refresh"))
        }

        override suspend fun refresh(): ApiResult<AuthResponse> =
            ApiResult.Success(AuthResponse("access", "refresh"))

        override fun logout() {
            logoutCalls += 1
        }

        override fun isLoggedIn(): Boolean = false
    }

    private class InMemoryTokenStorage(
        private var accessToken: String? = null,
        private var refreshToken: String? = null,
        private var email: String? = null,
    ) : ITokenStorage {
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

    private class FakeSyncActions : SyncActions {
        private val syncing = MutableStateFlow(false)
        private val morePages = MutableStateFlow(false)
        private val loadingMore = MutableStateFlow(false)
        var pullOnlyCalls = 0

        override val isSyncing: StateFlow<Boolean> = syncing.asStateFlow()
        override val hasMorePages: StateFlow<Boolean> = morePages.asStateFlow()
        override val isLoadingMore: StateFlow<Boolean> = loadingMore.asStateFlow()
        override fun sync() = Unit
        override fun pullOnly() { pullOnlyCalls += 1 }
        override fun loadMore() = Unit
        override fun syncCreate(localId: Long) = Unit
        override fun syncDelete(localId: Long) = Unit
    }

    private companion object {
        fun beer(id: Long) = Beer(id = id, name = "Beer $id", abv = 5.0, comment = null, rating = 4)
    }
}
