package ru.mikaeliv.beers.feature.list

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ru.mikaeliv.beers.core.Beer
import ru.mikaeliv.beers.core.SyncActions
import ru.mikaeliv.beers.core.SyncStatus
import ru.mikaeliv.beers.data.IBeerRepository
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BeerListComponentTest {

    /**
     * Проверяет, что компонент подписывается на список из репозитория и запускает первичный pull.
     */
    @Test
    fun initCollectsBeersAndStartsPullOnly() = runTest {
        val fixture = createFixture()
        val beers = listOf(beer(1L), beer(2L))

        fixture.repository.emit(beers)
        advanceUntilIdle()

        assertEquals(beers, fixture.component.state.value)
        assertEquals(1, fixture.syncActions.pullOnlyCalls)
    }

    /**
     * Проверяет, что клики навигации пробрасываются в output.
     */
    @Test
    fun navigationClicksCallOutput() = runTest {
        val fixture = createFixture()

        fixture.component.onAddClick()
        fixture.component.onOpen(42L)
        fixture.component.onProfileClick()

        assertEquals(1, fixture.output.openAddCalls)
        assertEquals(listOf(42L), fixture.output.openDetailIds)
        assertEquals(1, fixture.output.openProfileCalls)
    }

    /**
     * Проверяет, что refresh делегируется в SyncActions.
     */
    @Test
    fun refreshCallsSyncActions() = runTest {
        val fixture = createFixture()

        fixture.component.onRefresh()

        assertEquals(1, fixture.syncActions.syncCalls)
    }

    /**
     * Проверяет, что loadMore делегируется только если следующую страницу можно запросить.
     */
    @Test
    fun loadMoreCallsSyncActionsWhenNextPageCanBeRequested() = runTest {
        val fixture = createFixture()
        fixture.repository.emit(listOf(beer(1L), beer(2L)))
        fixture.syncActions.setHasMorePages(true)
        advanceUntilIdle()

        fixture.component.onLoadMore()

        assertEquals(1, fixture.syncActions.loadMoreCalls)
    }

    /**
     * Проверяет, что loadMore не повторяет запрос для того же размера списка.
     */
    @Test
    fun loadMoreSkipsRepeatedRequestForSameBeerCount() = runTest {
        val fixture = createFixture()
        fixture.repository.emit(listOf(beer(1L), beer(2L)))
        fixture.syncActions.setHasMorePages(true)
        advanceUntilIdle()

        fixture.component.onLoadMore()
        fixture.component.onLoadMore()

        assertEquals(1, fixture.syncActions.loadMoreCalls)
    }

    /**
     * Проверяет, что loadMore снова разрешён после появления новых элементов.
     */
    @Test
    fun loadMoreCanRequestAgainAfterBeerCountChanges() = runTest {
        val fixture = createFixture()
        fixture.syncActions.setHasMorePages(true)
        fixture.repository.emit(listOf(beer(1L), beer(2L)))
        advanceUntilIdle()

        fixture.component.onLoadMore()
        fixture.repository.emit(listOf(beer(1L), beer(2L), beer(3L)))
        advanceUntilIdle()
        fixture.component.onLoadMore()

        assertEquals(2, fixture.syncActions.loadMoreCalls)
    }

    /**
     * Проверяет, что loadMore не запускается без данных, без следующей страницы или во время загрузки.
     */
    @Test
    fun loadMoreSkipsWhenRequestIsNotAllowed() = runTest {
        val fixture = createFixture()

        fixture.component.onLoadMore()

        fixture.repository.emit(listOf(beer(1L)))
        fixture.syncActions.setHasMorePages(false)
        advanceUntilIdle()
        fixture.component.onLoadMore()

        fixture.syncActions.setHasMorePages(true)
        fixture.syncActions.setLoadingMore(true)
        fixture.component.onLoadMore()

        assertEquals(0, fixture.syncActions.loadMoreCalls)
    }

    /**
     * Проверяет, что state flow статусы компонента берутся напрямую из SyncActions.
     */
    @Test
    fun exposesSyncStateFlows() = runTest {
        val fixture = createFixture()

        fixture.syncActions.setSyncing(true)
        fixture.syncActions.setHasMorePages(true)
        fixture.syncActions.setLoadingMore(true)

        assertEquals(true, fixture.component.isRefreshing.value)
        assertEquals(true, fixture.component.hasMorePages.value)
        assertEquals(true, fixture.component.isLoadingMore.value)
    }

    /**
     * Проверяет, что onDestroy отменяет подписку на репозиторий и новые emission уже не меняют state.
     */
    @Test
    fun onDestroyStopsCollectingRepository() = runTest {
        val fixture = createFixture()
        val firstBeers = listOf(beer(1L))
        val secondBeers = listOf(beer(2L))

        fixture.repository.emit(firstBeers)
        advanceUntilIdle()
        assertEquals(firstBeers, fixture.component.state.value)

        fixture.component.onDestroy()
        fixture.repository.emit(secondBeers)
        advanceUntilIdle()

        assertEquals(firstBeers, fixture.component.state.value)
    }

    private fun TestScope.createFixture(): Fixture {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeBeerRepository()
        val syncActions = FakeSyncActions()
        val output = FakeOutput()
        val component = DefaultBeerListComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            repo = repository,
            syncEngine = syncActions,
            output = output,
            scope = CoroutineScope(dispatcher)
        )
        return Fixture(component, repository, syncActions, output)
    }

    private data class Fixture(
        val component: BeerListComponent,
        val repository: FakeBeerRepository,
        val syncActions: FakeSyncActions,
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

    private class FakeSyncActions : SyncActions {
        private val syncing = MutableStateFlow(false)
        private val morePages = MutableStateFlow(false)
        private val loadingMore = MutableStateFlow(false)
        var pullOnlyCalls = 0
        var syncCalls = 0
        var loadMoreCalls = 0
        override val isSyncing: StateFlow<Boolean> = syncing.asStateFlow()
        override val hasMorePages: StateFlow<Boolean> = morePages.asStateFlow()
        override val isLoadingMore: StateFlow<Boolean> = loadingMore.asStateFlow()
        override fun sync() { syncCalls += 1 }
        override fun pullOnly() { pullOnlyCalls += 1 }
        override fun loadMore() { loadMoreCalls += 1 }
        override fun syncCreate(localId: Long) = Unit
        override fun syncDelete(localId: Long) = Unit
        fun setSyncing(value: Boolean) { syncing.value = value }
        fun setHasMorePages(value: Boolean) { morePages.value = value }
        fun setLoadingMore(value: Boolean) { loadingMore.value = value }
    }

    private class FakeOutput : BeerListComponent.Output {
        var openAddCalls = 0
        var openProfileCalls = 0
        val openDetailIds = mutableListOf<Long>()
        override fun openAdd() { openAddCalls += 1 }
        override fun openDetail(id: Long) { openDetailIds += id }
        override fun openProfile() { openProfileCalls += 1 }
    }

    private fun beer(id: Long) = Beer(id = id, name = "Beer $id", abv = 5.0, comment = null, rating = 4)
}
