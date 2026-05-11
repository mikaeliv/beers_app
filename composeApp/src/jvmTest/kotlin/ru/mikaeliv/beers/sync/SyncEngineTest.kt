package ru.mikaeliv.beers.sync

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
import ru.mikaeliv.beers.network.api.IBeerApi
import ru.mikaeliv.beers.network.dto.BeerRequest
import ru.mikaeliv.beers.network.dto.BeerResponse
import ru.mikaeliv.beers.network.dto.BeersPageResponse
import ru.mikaeliv.beers.network.dto.PageMetadata
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncEngineTest {

    /**
     * Проверяет, что ручная синхронизация ничего не делает в offline-режиме.
     */
    @Test
    fun syncDoesNothingWhenOffline() = runTest {
        val fixture = createFixture(isOnline = false)

        fixture.engine.sync()
        advanceUntilIdle()

        assertEquals(0, fixture.repository.pendingCreateReads)
        assertTrue(fixture.api.getBeersCalls.isEmpty())
        assertFalse(fixture.engine.isSyncing.value)
    }

    /**
     * Проверяет syncCreate: локальная pending-запись отправляется на сервер и помечается SYNCED.
     */
    @Test
    fun syncCreateUploadsPendingBeerAndMarksItSynced() = runTest {
        val fixture = createFixture()
        fixture.repository.beersById[1L] = beer(id = 1L, syncStatus = SyncStatus.PENDING_CREATE)
        fixture.api.addBeerResults += ApiResult.Success(beerResponse(id = "server-1"))

        fixture.engine.syncCreate(1L)
        advanceUntilIdle()

        val request = fixture.api.addBeerRequests.single()
        assertEquals("Porter", request.request.name)
        assertContentEquals(byteArrayOf(1, 2, 3), request.imageBytes)
        assertEquals(UpdateSyncStatusCall(1L, "server-1", SyncStatus.SYNCED), fixture.repository.updateSyncStatusCalls.single())
    }

    /**
     * Проверяет syncCreate: запись без фото не отправляется, а ошибка остается в lastError.
     */
    @Test
    fun syncCreateKeepsPendingBeerWhenImageIsMissing() = runTest {
        val fixture = createFixture()
        fixture.repository.beersById[1L] = beer(id = 1L, photoBytes = null, syncStatus = SyncStatus.PENDING_CREATE)

        fixture.engine.syncCreate(1L)
        advanceUntilIdle()

        assertEquals("Image is required", fixture.engine.lastError.value)
        assertTrue(fixture.api.addBeerRequests.isEmpty())
        assertTrue(fixture.repository.updateSyncStatusCalls.isEmpty())
    }

    /**
     * Проверяет syncDelete: запись с serverId удаляется на сервере и затем физически из локальной БД.
     */
    @Test
    fun syncDeleteDeletesServerBeerAndThenLocalBeer() = runTest {
        val fixture = createFixture()
        fixture.repository.beersById[1L] = beer(
            id = 1L,
            serverId = "server-1",
            syncStatus = SyncStatus.PENDING_DELETE
        )

        fixture.engine.syncDelete(1L)
        advanceUntilIdle()

        assertEquals(listOf("server-1"), fixture.api.deleteBeerIds)
        assertEquals(listOf(1L), fixture.repository.deletedPhysicallyIds)
    }

    /**
     * Проверяет syncDelete: если serverId нет, локальную запись можно удалить без API-вызова.
     */
    @Test
    fun syncDeleteRemovesLocalBeerWithoutApiCallWhenServerIdIsMissing() = runTest {
        val fixture = createFixture()
        fixture.repository.beersById[1L] = beer(id = 1L, serverId = null, syncStatus = SyncStatus.PENDING_DELETE)

        fixture.engine.syncDelete(1L)
        advanceUntilIdle()

        assertTrue(fixture.api.deleteBeerIds.isEmpty())
        assertEquals(listOf(1L), fixture.repository.deletedPhysicallyIds)
    }

    /**
     * Проверяет pullOnly: движок очищает SYNCED-записи и загружает только первую страницу.
     */
    @Test
    fun pullOnlyDeletesSyncedAndLoadsFirstPage() = runTest {
        val fixture = createFixture()
        fixture.api.pages[0] = ApiResult.Success(
            pageResponse(
                page = 0,
                totalPages = 2,
                beerResponse(id = "server-1", imageUrl = "image-1")
            )
        )
        fixture.api.images["image-1"] = ApiResult.Success(byteArrayOf(7, 8, 9))

        fixture.engine.pullOnly()
        advanceUntilIdle()

        assertEquals(1, fixture.repository.deleteSyncedCalls)
        assertEquals(listOf(GetBeersCall(page = 0, size = 20)), fixture.api.getBeersCalls)
        val inserted = fixture.repository.insertedFromServer.single()
        assertEquals("server-1", inserted.serverId)
        assertContentEquals(byteArrayOf(7, 8, 9), inserted.photoBytes)
        assertTrue(fixture.engine.hasMorePages.value)
    }

    /**
     * Проверяет loadMore: следующая страница загружается, вставляется в БД и обновляет hasMorePages.
     */
    @Test
    fun loadMoreLoadsNextPageAndUpdatesPaginationState() = runTest {
        val fixture = createFixture()
        fixture.api.pages[0] = ApiResult.Success(
            pageResponse(
                page = 0,
                totalPages = 1,
                beerResponse(id = "server-1", imageUrl = "image-1")
            )
        )
        fixture.api.images["image-1"] = ApiResult.Success(byteArrayOf(4, 5, 6))

        fixture.engine.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(GetBeersCall(page = 0, size = 20)), fixture.api.getBeersCalls)
        assertEquals("server-1", fixture.repository.insertedFromServer.single().serverId)
        assertFalse(fixture.engine.hasMorePages.value)
        assertFalse(fixture.engine.isLoadingMore.value)
    }

    /**
     * Проверяет sync: pending create/delete отправляются на сервер, затем выполняется pull всех страниц.
     */
    @Test
    fun syncPushesPendingChangesAndPullsAllPages() = runTest {
        val fixture = createFixture()
        fixture.repository.pendingCreate += beer(id = 1L, syncStatus = SyncStatus.PENDING_CREATE)
        fixture.repository.pendingDelete += beer(id = 2L, serverId = "server-delete", syncStatus = SyncStatus.PENDING_DELETE)
        fixture.api.addBeerResults += ApiResult.Success(beerResponse(id = "server-created"))
        fixture.api.pages[0] = ApiResult.Success(pageResponse(page = 0, totalPages = 2, beerResponse(id = "server-page-0")))
        fixture.api.pages[1] = ApiResult.Success(pageResponse(page = 1, totalPages = 2, beerResponse(id = "server-page-1")))

        fixture.engine.sync()
        advanceUntilIdle()

        assertEquals(UpdateSyncStatusCall(1L, "server-created", SyncStatus.SYNCED), fixture.repository.updateSyncStatusCalls.single())
        assertEquals(listOf("server-delete"), fixture.api.deleteBeerIds)
        assertEquals(listOf(2L), fixture.repository.deletedPhysicallyIds)
        assertEquals(listOf(GetBeersCall(0, 20), GetBeersCall(1, 20)), fixture.api.getBeersCalls)
        assertEquals(listOf("server-page-0", "server-page-1"), fixture.repository.insertedFromServer.map { it.serverId })
    }

    /**
     * Проверяет автоматический push pending-изменений при переходе сети из offline в online.
     */
    @Test
    fun onlineRestorePushesPendingChanges() = runTest {
        val onlineState = MutableStateFlow(false)
        val fixture = createFixture(onlineState = onlineState, observeOnlineChanges = true)
        fixture.repository.pendingCreate += beer(id = 1L, syncStatus = SyncStatus.PENDING_CREATE)
        fixture.api.addBeerResults += ApiResult.Success(beerResponse(id = "server-created"))

        onlineState.value = true
        advanceUntilIdle()

        assertEquals(UpdateSyncStatusCall(1L, "server-created", SyncStatus.SYNCED), fixture.repository.updateSyncStatusCalls.single())
    }

    private fun TestScope.createFixture(
        isOnline: Boolean = true,
        onlineState: MutableStateFlow<Boolean> = MutableStateFlow(isOnline),
        observeOnlineChanges: Boolean = false,
    ): Fixture {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeBeerRepository()
        val api = FakeBeerApi()
        val engine = SyncEngine(
            repository = repository,
            beerApi = api,
            scope = CoroutineScope(dispatcher),
            workDispatcher = dispatcher,
            observeOnlineChanges = observeOnlineChanges,
            onlineState = onlineState
        )

        return Fixture(
            engine = engine,
            repository = repository,
            api = api
        )
    }

    private data class Fixture(
        val engine: SyncEngine,
        val repository: FakeBeerRepository,
        val api: FakeBeerApi,
    )

    private class FakeBeerRepository : IBeerRepository {
        val beersById = mutableMapOf<Long, Beer>()
        val beersByServerId = mutableMapOf<String, Beer>()
        val pendingCreate = mutableListOf<Beer>()
        val pendingDelete = mutableListOf<Beer>()
        val updateSyncStatusCalls = mutableListOf<UpdateSyncStatusCall>()
        val deletedPhysicallyIds = mutableListOf<Long>()
        val insertedFromServer = mutableListOf<InsertFromServerCall>()
        var deleteSyncedCalls = 0
        var pendingCreateReads = 0

        override fun getAll(): Flow<List<Beer>> = MutableStateFlow(emptyList())

        override suspend fun getById(id: Long): Beer? = beersById[id]

        override suspend fun getByServerId(serverId: String): Beer? = beersByServerId[serverId]

        override suspend fun add(beer: Beer): Long = error("Not used by SyncEngine")

        override suspend fun insertFromServer(
            serverId: String,
            name: String,
            abv: Double,
            rating: Int,
            comment: String?,
            photoBytes: ByteArray?,
        ): Long {
            insertedFromServer += InsertFromServerCall(serverId, name, abv, rating, comment, photoBytes)
            beersByServerId[serverId] = Beer(
                id = insertedFromServer.size.toLong(),
                serverId = serverId,
                name = name,
                abv = abv,
                comment = comment,
                rating = rating,
                photoBytes = photoBytes,
                syncStatus = SyncStatus.SYNCED
            )
            return insertedFromServer.size.toLong()
        }

        override suspend fun update(beer: Beer) = Unit

        override suspend fun delete(id: Long) = Unit

        override suspend fun getPendingCreate(): List<Beer> {
            pendingCreateReads += 1
            return pendingCreate
        }

        override suspend fun getPendingDelete(): List<Beer> = pendingDelete

        override suspend fun updateSyncStatus(id: Long, serverId: String?, status: SyncStatus) {
            updateSyncStatusCalls += UpdateSyncStatusCall(id, serverId, status)
        }

        override suspend fun deletePhysically(id: Long) {
            deletedPhysicallyIds += id
        }

        override suspend fun deleteSynced() {
            deleteSyncedCalls += 1
        }

        override suspend fun deleteAll() = Unit
    }

    private class FakeBeerApi : IBeerApi {
        val addBeerRequests = mutableListOf<AddBeerCall>()
        val addBeerResults = mutableListOf<ApiResult<BeerResponse>>()
        val deleteBeerIds = mutableListOf<String>()
        val getBeersCalls = mutableListOf<GetBeersCall>()
        val pages = mutableMapOf<Int, ApiResult<BeersPageResponse>>()
        val images = mutableMapOf<String, ApiResult<ByteArray>>()

        override suspend fun getBeers(page: Int, size: Int): ApiResult<BeersPageResponse> {
            getBeersCalls += GetBeersCall(page, size)
            return pages[page] ?: ApiResult.Success(
                BeersPageResponse(
                    content = emptyList(),
                    page = PageMetadata(
                        size = size,
                        number = page,
                        totalElements = 0,
                        totalPages = page + 1
                    )
                )
            )
        }

        override suspend fun addBeer(request: BeerRequest, imageBytes: ByteArray): ApiResult<BeerResponse> {
            addBeerRequests += AddBeerCall(request, imageBytes)
            return if (addBeerResults.isNotEmpty()) {
                addBeerResults.removeAt(0)
            } else {
                ApiResult.Success(
                    BeerResponse(
                        id = "server-${addBeerRequests.size}",
                        name = request.name,
                        rating = request.rating,
                        abv = request.abv,
                        description = request.description,
                        imageUrl = "image-${addBeerRequests.size}",
                        createdAt = "2026-05-11T00:00:00Z"
                    )
                )
            }
        }

        override suspend fun updateBeer(request: BeerRequest, imageBytes: ByteArray?): ApiResult<BeerResponse> =
            error("Not used by SyncEngine")

        override suspend fun getBeerImage(imageUrl: String): ApiResult<ByteArray> =
            images[imageUrl] ?: ApiResult.Success(byteArrayOf(1, 2, 3))

        override suspend fun deleteBeer(id: String): ApiResult<Unit> {
            deleteBeerIds += id
            return ApiResult.Success(Unit)
        }
    }

    private data class AddBeerCall(val request: BeerRequest, val imageBytes: ByteArray)
    private data class GetBeersCall(val page: Int, val size: Int)
    private data class UpdateSyncStatusCall(val id: Long, val serverId: String?, val status: SyncStatus)
    private data class InsertFromServerCall(
        val serverId: String,
        val name: String,
        val abv: Double,
        val rating: Int,
        val comment: String?,
        val photoBytes: ByteArray?,
    )

    private fun beer(
        id: Long?,
        serverId: String? = null,
        name: String = "Porter",
        abv: Double = 5.5,
        comment: String? = "Nice",
        rating: Int = 4,
        photoBytes: ByteArray? = byteArrayOf(1, 2, 3),
        syncStatus: SyncStatus = SyncStatus.SYNCED,
    ) = Beer(
        id = id,
        serverId = serverId,
        name = name,
        abv = abv,
        comment = comment,
        rating = rating,
        photoBytes = photoBytes,
        syncStatus = syncStatus
    )

    private fun beerResponse(
        id: String,
        name: String = "Server Beer",
        imageUrl: String = "image-$id",
    ) = BeerResponse(
        id = id,
        name = name,
        rating = 4,
        abv = 5.0,
        description = "Server description",
        imageUrl = imageUrl,
        createdAt = "2026-05-11T00:00:00Z"
    )

    private fun pageResponse(
        page: Int,
        totalPages: Int,
        vararg beers: BeerResponse,
    ) = BeersPageResponse(
        content = beers.toList(),
        page = PageMetadata(
            size = 20,
            number = page,
            totalElements = beers.size,
            totalPages = totalPages
        )
    )
}
