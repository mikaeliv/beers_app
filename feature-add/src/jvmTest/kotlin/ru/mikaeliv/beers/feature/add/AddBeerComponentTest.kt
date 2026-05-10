package ru.mikaeliv.beers.feature.add

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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddBeerComponentTest {

    /**
     * Проверяет начальное состояние формы: поля пустые, сохранение не идет, форма невалидна.
     */
    @Test
    fun initialStateIsEmptyAndInvalid() = runTest {
        val fixture = createFixture()

        val state = fixture.component.state.value

        assertEquals("", state.name)
        assertEquals("", state.abv)
        assertEquals(1, state.rating)
        assertEquals("", state.comment)
        assertNull(state.photoBytes)
        assertFalse(state.isSaving)
        assertFalse(state.isValid)
    }

    /**
     * Проверяет, что изменение полей обновляет state, но форма становится валидной только после фото.
     */
    @Test
    fun formBecomesValidOnlyWhenRequiredFieldsAreFilled() = runTest {
        val fixture = createFixture()

        fixture.component.onNameChange("Porter")
        fixture.component.onAbvChange("5.7")
        fixture.component.onRatingChange(4)
        fixture.component.onCommentChange("Coffee notes")

        assertFalse(fixture.component.state.value.isValid)

        fixture.component.onPhotoSelected(byteArrayOf(1, 2, 3))

        val state = fixture.component.state.value
        assertEquals("Porter", state.name)
        assertEquals("5.7", state.abv)
        assertEquals(4, state.rating)
        assertEquals("Coffee notes", state.comment)
        assertContentEquals(byteArrayOf(1, 2, 3), state.photoBytes)
        assertTrue(state.isValid)
    }

    /**
     * Проверяет, что рейтинг принудительно остается в диапазоне 1..5.
     */
    @Test
    fun ratingIsClampedToAllowedRange() = runTest {
        val fixture = createFixture()

        fixture.component.onRatingChange(0)
        assertEquals(1, fixture.component.state.value.rating)

        fixture.component.onRatingChange(6)
        assertEquals(5, fixture.component.state.value.rating)
    }

    /**
     * Проверяет, что кнопка "назад" пробрасывает событие наружу через output.
     */
    @Test
    fun onBackCallsOutputBack() = runTest {
        val fixture = createFixture()

        fixture.component.onBack()

        assertEquals(1, fixture.output.backCalls)
    }

    /**
     * Проверяет, что невалидная форма не сохраняется и не запускает синхронизацию.
     */
    @Test
    fun onSaveDoesNothingWhenFormIsInvalid() = runTest {
        val fixture = createFixture()
        fixture.component.onNameChange("IPA")
        fixture.component.onAbvChange("6.2")

        fixture.component.onSave()
        advanceUntilIdle()

        assertFalse(fixture.component.state.value.isSaving)
        assertTrue(fixture.repository.addedBeers.isEmpty())
        assertTrue(fixture.syncActions.syncCreateIds.isEmpty())
        assertEquals(0, fixture.output.savedCalls)
    }

    /**
     * Проверяет успешное сохранение: компонент добавляет Beer, запускает syncCreate и сообщает output.saved.
     */
    @Test
    fun onSaveAddsBeerStartsSyncAndCallsSavedOutput() = runTest {
        val fixture = createFixture(nextBeerId = 42L)
        fixture.component.onNameChange("Stout")
        fixture.component.onAbvChange("7.5")
        fixture.component.onRatingChange(5)
        fixture.component.onCommentChange("Roasted")
        fixture.component.onPhotoSelected(byteArrayOf(9, 8, 7))

        fixture.component.onSave()
        advanceUntilIdle()

        val savedBeer = fixture.repository.addedBeers.single()
        assertEquals("Stout", savedBeer.name)
        assertEquals(7.5, savedBeer.abv)
        assertEquals(5, savedBeer.rating)
        assertEquals("Roasted", savedBeer.comment)
        assertContentEquals(byteArrayOf(9, 8, 7), savedBeer.photoBytes)
        assertEquals(listOf(42L), fixture.syncActions.syncCreateIds)
        assertEquals(1, fixture.output.savedCalls)
    }

    /**
     * Проверяет, что пустой комментарий при сохранении превращается в null в доменной модели.
     */
    @Test
    fun onSaveStoresBlankCommentAsNull() = runTest {
        val fixture = createFixture()
        fixture.component.onNameChange("Lager")
        fixture.component.onAbvChange("4.8")
        fixture.component.onRatingChange(3)
        fixture.component.onCommentChange("   ")
        fixture.component.onPhotoSelected(byteArrayOf(1))

        fixture.component.onSave()
        advanceUntilIdle()

        assertNull(fixture.repository.addedBeers.single().comment)
    }

    private fun TestScope.createFixture(nextBeerId: Long = 1L): Fixture {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeBeerRepository(nextBeerId)
        val syncActions = FakeSyncActions()
        val output = FakeOutput()
        val component = DefaultAddBeerComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            repo = repository,
            syncActions = syncActions,
            output = output,
            scope = CoroutineScope(dispatcher),
            databaseDispatcher = dispatcher
        )

        return Fixture(
            component = component,
            repository = repository,
            syncActions = syncActions,
            output = output
        )
    }

    /**
     * Собирает компонент и все fake-зависимости, чтобы тест мог проверять и состояние UI, и побочные вызовы.
     */
    private data class Fixture(
        val component: AddBeerComponent,
        val repository: FakeBeerRepository,
        val syncActions: FakeSyncActions,
        val output: FakeOutput,
    )

    private class FakeBeerRepository(private val nextBeerId: Long) : IBeerRepository {
        val addedBeers = mutableListOf<Beer>()
        private val beers = MutableStateFlow<List<Beer>>(emptyList())

        override fun getAll(): Flow<List<Beer>> = beers

        override suspend fun getById(id: Long): Beer? = null

        override suspend fun getByServerId(serverId: String): Beer? = null

        override suspend fun add(beer: Beer): Long {
            addedBeers += beer
            return nextBeerId
        }

        override suspend fun insertFromServer(
            serverId: String,
            name: String,
            abv: Double,
            rating: Int,
            comment: String?,
            photoBytes: ByteArray?,
        ): Long = nextBeerId

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
        val syncCreateIds = mutableListOf<Long>()
        private val syncing = MutableStateFlow(false)
        private val morePages = MutableStateFlow(false)
        private val loadingMore = MutableStateFlow(false)

        override val isSyncing: StateFlow<Boolean> = syncing.asStateFlow()
        override val hasMorePages: StateFlow<Boolean> = morePages.asStateFlow()
        override val isLoadingMore: StateFlow<Boolean> = loadingMore.asStateFlow()

        override fun sync() = Unit

        override fun pullOnly() = Unit

        override fun loadMore() = Unit

        override fun syncCreate(localId: Long) {
            syncCreateIds += localId
        }

        override fun syncDelete(localId: Long) = Unit
    }

    private class FakeOutput : AddBeerComponent.Output {
        var savedCalls = 0
        var backCalls = 0

        override fun saved() {
            savedCalls += 1
        }

        override fun back() {
            backCalls += 1
        }
    }
}
