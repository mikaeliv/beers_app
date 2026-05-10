package ru.mikaeliv.beers.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.mikaeliv.beers.core.Beer
import ru.mikaeliv.beers.core.SyncStatus
import ru.mikaeliv.beers.db.Beer as DbBeer
import ru.mikaeliv.beers.db.BeerQueries
import ru.mikaeliv.beers.db.BeersDatabase

/**
 * Контракт репозитория для работы с пивом.
 * Компоненты зависят от этого интерфейса, поэтому в тестах можно подставлять fake-реализации.
 */
interface IBeerRepository {
    fun getAll(): Flow<List<Beer>>
    suspend fun getById(id: Long): Beer?
    suspend fun getByServerId(serverId: String): Beer?
    suspend fun add(beer: Beer): Long
    suspend fun insertFromServer(
        serverId: String,
        name: String,
        abv: Double,
        rating: Int,
        comment: String?,
        photoBytes: ByteArray? = null,
    ): Long
    suspend fun update(beer: Beer)
    suspend fun delete(id: Long)
    suspend fun getPendingCreate(): List<Beer>
    suspend fun getPendingDelete(): List<Beer>
    suspend fun updateSyncStatus(id: Long, serverId: String?, status: SyncStatus)
    suspend fun deletePhysically(id: Long)
    suspend fun deleteSynced()
    suspend fun deleteAll()
}

/**
 * Репозиторий для работы с пивом в базе данных.
 * Поддерживает offline-first архитектуру с синхронизацией.
 */
class BeerRepositoryImpl(private val database: BeersDatabase) : IBeerRepository {
    private val queries: BeerQueries = database.beerQueries

    /**
     * Получить все записи пива как Flow.
     * Исключает записи со статусом PENDING_DELETE.
     */
    override fun getAll(): Flow<List<Beer>> = queries.getAll()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { dbBeers -> dbBeers.map { it.toBeer() } }

    /**
     * Получить пиво по локальному id.
     */
    override suspend fun getById(id: Long): Beer? {
        return queries.getById(id).executeAsOneOrNull()?.toBeer()
    }

    /**
     * Получить пиво по server id.
     */
    override suspend fun getByServerId(serverId: String): Beer? {
        return queries.getByServerId(serverId).executeAsOneOrNull()?.toBeer()
    }

    /**
     * Добавить новое пиво локально (со статусом PENDING_CREATE).
     * @return локальный id созданной записи
     */
    override suspend fun add(beer: Beer): Long {
        queries.insertBeer(
            name = beer.name,
            abv = beer.abv,
            comment = beer.comment,
            rating = beer.rating.toLong(),
            photo = beer.photoBytes,
            sync_status = SyncStatus.PENDING_CREATE.name
        )
        return queries.lastInsertRowId().executeAsOne()
    }

    /**
     * Добавить пиво с сервера (со статусом SYNCED).
     */
    override suspend fun insertFromServer(
        serverId: String,
        name: String,
        abv: Double,
        rating: Int,
        comment: String?,
        photoBytes: ByteArray?,
    ): Long {
        queries.transaction {
            queries.insertFromServer(
                server_id = serverId,
                name = name,
                abv = abv,
                comment = comment,
                rating = rating.toLong(),
                photo = photoBytes
            )
            queries.updateFromServer(
                name = name,
                abv = abv,
                comment = comment,
                rating = rating.toLong(),
                photo = photoBytes,
                server_id = serverId
            )
        }
        return getByServerId(serverId)?.id ?: queries.lastInsertRowId().executeAsOne()
    }

    /**
     * Обновить существующее пиво.
     */
    override suspend fun update(beer: Beer) {
        val beerId = beer.id ?: throw IllegalArgumentException("Beer id must not be null for update")
        queries.updateBeer(
            name = beer.name,
            abv = beer.abv,
            comment = beer.comment,
            rating = beer.rating.toLong(),
            photo = beer.photoBytes,
            id = beerId
        )
    }

    /**
     * Пометить пиво как удалённое (для последующей синхронизации).
     * Если запись ещё не была синхронизирована (PENDING_CREATE), удаляем сразу.
     */
    override suspend fun delete(id: Long) {
        val beer = getById(id) ?: return
        if (beer.syncStatus == SyncStatus.PENDING_CREATE) {
            // Ещё не отправлялось на сервер — можно удалить сразу
            queries.deleteBeer(id)
        } else {
            // Нужно удалить на сервере — помечаем
            queries.markAsDeleted(id)
        }
    }

    /**
     * Получить записи, ожидающие создания на сервере.
     */
    override suspend fun getPendingCreate(): List<Beer> {
        return queries.getPendingCreate().executeAsList().map { it.toBeer() }
    }

    /**
     * Получить записи, ожидающие удаления на сервере.
     */
    override suspend fun getPendingDelete(): List<Beer> {
        return queries.getPendingDelete().executeAsList().map { it.toBeer() }
    }

    /**
     * Обновить статус синхронизации записи.
     */
    override suspend fun updateSyncStatus(id: Long, serverId: String?, status: SyncStatus) {
        queries.updateSyncStatus(
            server_id = serverId,
            sync_status = status.name,
            id = id
        )
    }

    /**
     * Физически удалить запись (после успешной синхронизации удаления).
     */
    override suspend fun deletePhysically(id: Long) {
        queries.deleteBeer(id)
    }

    /**
     * Удалить все синхронизированные записи (перед полным pull с сервера).
     */
    override suspend fun deleteSynced() {
        queries.deleteSynced()
    }

    /**
     * Удалить все записи.
     */
    override suspend fun deleteAll() {
        queries.deleteAll()
    }

    private fun DbBeer.toBeer() = Beer(
        id = id,
        serverId = server_id,
        name = name,
        abv = abv,
        comment = comment,
        rating = rating.toInt(),
        photoBytes = photo,
        syncStatus = SyncStatus.fromString(sync_status)
    )
}
