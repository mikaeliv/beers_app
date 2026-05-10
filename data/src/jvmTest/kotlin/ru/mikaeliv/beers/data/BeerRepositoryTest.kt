package ru.mikaeliv.beers.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ru.mikaeliv.beers.core.Beer
import ru.mikaeliv.beers.core.SyncStatus
import ru.mikaeliv.beers.db.BeersDatabase
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Проверяет BeerRepository на настоящей SQLDelight-схеме, но в in-memory SQLite базе.
 * Так тесты остаются быстрыми и при этом ловят ошибки в SQL-запросах и маппинге данных.
 */
class BeerRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var repository: IBeerRepository

    /**
     * Закрывает in-memory SQLite драйвер после каждого теста, чтобы не держать лишние ресурсы.
     */
    @AfterTest
    fun tearDown() {
        driver.close()
    }

    /**
     * Проверяет, что новое локально добавленное пиво попадает в очередь создания на сервере.
     */
    @Test
    fun addCreatesBeerWithPendingCreateStatus() = runTest {
        createRepository()

        // Новая локальная запись должна ждать отправки на сервер.
        val id = repository.add(newBeer(name = "Local IPA"))

        val beer = repository.getById(id)
        assertNotNull(beer)
        assertEquals("Local IPA", beer.name)
        assertEquals(SyncStatus.PENDING_CREATE, beer.syncStatus)
        assertNull(beer.serverId)
    }

    /**
     * Проверяет, что список для отображения скрывает записи, помеченные на удаление.
     */
    @Test
    fun getAllDoesNotReturnPendingDeleteBeers() = runTest {
        createRepository()
        // Список для UI не должен показывать записи, которые пользователь уже удалил локально.
        val visibleId = repository.insertFromServer(
            serverId = "server-visible",
            name = "Visible Lager",
            abv = 4.8,
            rating = 4,
            comment = null
        )
        val deletedId = repository.insertFromServer(
            serverId = "server-deleted",
            name = "Deleted Stout",
            abv = 6.1,
            rating = 3,
            comment = null
        )

        repository.delete(deletedId)

        val beers = repository.getAll().first()
        assertEquals(listOf(visibleId), beers.map { it.id })
        assertTrue(beers.none { it.syncStatus == SyncStatus.PENDING_DELETE })
    }

    /**
     * Проверяет, что запись, загруженная с сервера впервые, сохраняется как синхронизированная.
     */
    @Test
    fun insertFromServerCreatesNewSyncedBeer() = runTest {
        createRepository()

        // Данные, пришедшие с сервера, сразу считаются синхронизированными.
        val id = repository.insertFromServer(
            serverId = "server-1",
            name = "Server Pilsner",
            abv = 5.0,
            rating = 5,
            comment = "Fresh"
        )

        val beer = repository.getById(id)
        assertNotNull(beer)
        assertEquals("server-1", beer.serverId)
        assertEquals("Server Pilsner", beer.name)
        assertEquals(SyncStatus.SYNCED, beer.syncStatus)
    }

    /**
     * Проверяет, что повторная загрузка с тем же server_id обновляет существующую запись.
     */
    @Test
    fun insertFromServerUpdatesExistingBeerByServerId() = runTest {
        createRepository()
        // Повторный pull с тем же server_id должен обновлять запись, а не плодить дубликаты.
        val firstId = repository.insertFromServer(
            serverId = "server-1",
            name = "Old Name",
            abv = 4.0,
            rating = 2,
            comment = "Old"
        )

        val secondId = repository.insertFromServer(
            serverId = "server-1",
            name = "New Name",
            abv = 6.5,
            rating = 5,
            comment = "New"
        )

        val beer = repository.getByServerId("server-1")
        assertNotNull(beer)
        assertEquals(firstId, secondId)
        assertEquals(firstId, beer.id)
        assertEquals("New Name", beer.name)
        assertEquals(6.5, beer.abv)
        assertEquals(5, beer.rating)
        assertEquals("New", beer.comment)
    }

    /**
     * Проверяет, что несинхронизированная локальная запись удаляется физически.
     */
    @Test
    fun deletePhysicallyRemovesPendingCreateBeer() = runTest {
        createRepository()
        // Если запись еще не ушла на сервер, ее можно удалить из локальной БД сразу.
        val id = repository.add(newBeer(name = "Draft"))

        repository.delete(id)

        assertNull(repository.getById(id))
    }

    /**
     * Проверяет, что синхронизированная запись не удаляется сразу, а ставится в очередь удаления.
     */
    @Test
    fun deleteMarksSyncedBeerAsPendingDelete() = runTest {
        createRepository()
        // Синхронизированную запись нельзя удалить сразу: сервер еще должен получить delete.
        val id = repository.insertFromServer(
            serverId = "server-1",
            name = "Synced Porter",
            abv = 5.7,
            rating = 4,
            comment = null
        )

        repository.delete(id)

        val beer = repository.getById(id)
        assertNotNull(beer)
        assertEquals(SyncStatus.PENDING_DELETE, beer.syncStatus)
    }

    /**
     * Проверяет, что очередь создания содержит только записи со статусом PENDING_CREATE.
     */
    @Test
    fun getPendingCreateReturnsOnlyPendingCreateBeers() = runTest {
        createRepository()
        // Очередь создания нужна SyncEngine, чтобы отправлять только новые локальные записи.
        val pendingId = repository.add(newBeer(name = "Local Wheat"))
        repository.insertFromServer(
            serverId = "server-1",
            name = "Synced Wheat",
            abv = 5.2,
            rating = 4,
            comment = null
        )

        val pending = repository.getPendingCreate()

        assertEquals(listOf(pendingId), pending.map { it.id })
    }

    /**
     * Проверяет, что очередь удаления содержит только записи со статусом PENDING_DELETE.
     */
    @Test
    fun getPendingDeleteReturnsOnlyPendingDeleteBeers() = runTest {
        createRepository()
        // Очередь удаления должна содержать только записи, которые уже скрыты локально.
        val pendingDeleteId = repository.insertFromServer(
            serverId = "server-delete",
            name = "Old Ale",
            abv = 7.0,
            rating = 3,
            comment = null
        )
        repository.insertFromServer(
            serverId = "server-synced",
            name = "Fresh Ale",
            abv = 5.5,
            rating = 4,
            comment = null
        )

        repository.delete(pendingDeleteId)

        val pending = repository.getPendingDelete()
        assertEquals(listOf(pendingDeleteId), pending.map { it.id })
    }

    /**
     * Проверяет, что очистка синхронизированных записей не удаляет локальные pending-изменения.
     */
    @Test
    fun deleteSyncedKeepsPendingBeers() = runTest {
        createRepository()
        // При полном pull можно чистить SYNCED-записи, но pending-изменения терять нельзя.
        val pendingCreateId = repository.add(newBeer(name = "Local Sour"))
        val pendingDeleteId = repository.insertFromServer(
            serverId = "server-delete",
            name = "Remote Sour",
            abv = 4.9,
            rating = 3,
            comment = null
        )
        val syncedId = repository.insertFromServer(
            serverId = "server-synced",
            name = "Synced Sour",
            abv = 5.1,
            rating = 4,
            comment = null
        )
        repository.delete(pendingDeleteId)

        repository.deleteSynced()

        assertNotNull(repository.getById(pendingCreateId))
        assertNotNull(repository.getById(pendingDeleteId))
        assertNull(repository.getById(syncedId))
    }

    /**
     * Создает чистый репозиторий с новой in-memory SQLDelight базой для отдельного теста.
     */
    private fun createRepository() {
        // Каждый тест получает новую чистую БД, чтобы сценарии не влияли друг на друга.
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BeersDatabase.Schema.create(driver)
        repository = BeerRepositoryImpl(BeersDatabase(driver))
    }

    /**
     * Собирает минимальную валидную доменную модель пива для тестов добавления.
     */
    private fun newBeer(name: String) = Beer(
        id = null,
        name = name,
        abv = 5.0,
        comment = null,
        rating = 4,
        photoBytes = byteArrayOf(1, 2, 3)
    )
}
