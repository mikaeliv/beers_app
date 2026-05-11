package ru.mikaeliv.beers.feature.detail

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
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class BeerDetailComponentTest {

    /**
     * Проверяет, что компонент загружает пиво по id при создании.
     */
    @Test
    fun loadsBeerOnInit() = runTest {
        val fixture = createFixture(beer = beer(id = 10L))

        advanceUntilIdle()

        assertFalse(fixture.component.state.value.isLoading)
        assertEquals(10L, fixture.component.state.value.beer?.id)
        assertEquals(listOf(10L), fixture.repository.getByIdCalls)
    }

    /**
     * Проверяет, что отсутствие записи выключает loading и оставляет beer null.
     */
    @Test
    fun keepsBeerNullWhenRepositoryReturnsNull() = runTest {
        val fixture = createFixture(beer = null)

        advanceUntilIdle()

        assertFalse(fixture.component.state.value.isLoading)
        assertNull(fixture.component.state.value.beer)
    }

    /**
     * Проверяет, что back пробрасывается наружу через output.
     */
    @Test
    fun onBackCallsOutputBack() = runTest {
        val fixture = createFixture(beer = beer(id = 10L))

        fixture.component.onBack()

        assertEquals(1, fixture.output.backCalls)
    }

    /**
     * Проверяет удаление: репозиторий помечает запись, syncDelete запускается, экран закрывается.
     */
    @Test
    fun onDeleteDeletesBeerStartsSyncAndGoesBack() = runTest {
        val fixture = createFixture(beer = beer(id = 10L))
        advanceUntilIdle()

        fixture.component.onDelete()
        advanceUntilIdle()

        assertEquals(listOf(10L), fixture.repository.deleteCalls)
        assertEquals(listOf(10L), fixture.syncActions.syncDeleteIds)
        assertEquals(1, fixture.output.backCalls)
    }

    /**
     * Проверяет, что delete ничего не делает, если пиво не загружено.
     */
    @Test
    fun onDeleteDoesNothingWhenBeerIsMissing() = runTest {
        val fixture = createFixture(beer = null)
        advanceUntilIdle()

        fixture.component.onDelete()
        advanceUntilIdle()

        assertEquals(emptyList(), fixture.repository.deleteCalls)
        assertEquals(emptyList(), fixture.syncActions.syncDeleteIds)
        assertEquals(0, fixture.output.backCalls)
    }

    private fun TestScope.createFixture(beer: Beer?): Fixture {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeBeerRepository(beer)
        val syncActions = FakeSyncActions()
        val output = FakeOutput()
        val component = DefaultBeerDetailComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            repo = repository,
            syncActions = syncActions,
            beerId = 10L,
            output = output,
            scope = CoroutineScope(dispatcher),
            workDispatcher = dispatcher
        )
        return Fixture(component, repository, syncActions, output)
    }

    private data class Fixture(
        val component: BeerDetailComponent,
        val repository: FakeBeerRepository,
        val syncActions: FakeSyncActions,
        val output: FakeOutput,
    )

    private class FakeBeerRepository(private val beer: Beer?) : IBeerRepository {
        val getByIdCalls = mutableListOf<Long>()
        val deleteCalls = mutableListOf<Long>()

        override fun getAll(): Flow<List<Beer>> = MutableStateFlow(emptyList())
        override suspend fun getById(id: Long): Beer? {
            getByIdCalls += id
            return beer
        }
        override suspend fun getByServerId(serverId: String): Beer? = null
        override suspend fun add(beer: Beer): Long = 0L
        override suspend fun insertFromServer(serverId: String, name: String, abv: Double, rating: Int, comment: String?, photoBytes: ByteArray?): Long = 0L
        override suspend fun update(beer: Beer) = Unit
        override suspend fun delete(id: Long) { deleteCalls += id }
        override suspend fun getPendingCreate(): List<Beer> = emptyList()
        override suspend fun getPendingDelete(): List<Beer> = emptyList()
        override suspend fun updateSyncStatus(id: Long, serverId: String?, status: SyncStatus) = Unit
        override suspend fun deletePhysically(id: Long) = Unit
        override suspend fun deleteSynced() = Unit
        override suspend fun deleteAll() = Unit
    }

    private class FakeSyncActions : SyncActions {
        val syncDeleteIds = mutableListOf<Long>()
        override val isSyncing: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
        override val hasMorePages: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
        override val isLoadingMore: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
        override fun sync() = Unit
        override fun pullOnly() = Unit
        override fun loadMore() = Unit
        override fun syncCreate(localId: Long) = Unit
        override fun syncDelete(localId: Long) { syncDeleteIds += localId }
    }

    private class FakeOutput : BeerDetailComponent.Output {
        var backCalls = 0
        override fun back() { backCalls += 1 }
    }

    private fun beer(id: Long) = Beer(id = id, name = "Porter", abv = 5.5, comment = null, rating = 4)
}
