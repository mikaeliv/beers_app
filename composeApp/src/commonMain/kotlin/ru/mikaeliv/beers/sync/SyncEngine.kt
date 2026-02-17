package ru.mikaeliv.beers.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mikaeliv.beers.network.connectivity.NetworkState
import ru.mikaeliv.beers.core.SyncActions
import ru.mikaeliv.beers.core.SyncStatus
import ru.mikaeliv.beers.data.BeerRepository
import ru.mikaeliv.beers.network.api.BeerApi
import ru.mikaeliv.beers.network.dto.BeerRequest

/**
 * Движок синхронизации данных между локальной БД и сервером.
 * Реализует offline-first архитектуру с автоматической синхронизацией
 * при восстановлении соединения.
 */
class SyncEngine(
    private val repository: BeerRepository,
    private val beerApi: BeerApi = BeerApi(),
) : SyncActions {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    /** Состояние сети (online/offline). */
    val isOnline: StateFlow<Boolean>
        get() = if (NetworkState.isInitialized) NetworkState.isOnline else MutableStateFlow(true)

    init {
        // Подписываемся на изменения состояния сети
        scope.launch {
            if (NetworkState.isInitialized) {
                NetworkState.isOnline.collectLatest { online ->
                    if (online) {
                        // При восстановлении соединения — синхронизируем
                        syncPendingChanges()
                    }
                }
            }
        }
    }

    /**
     * Запускает полную синхронизацию:
     * 1. Push локальных изменений на сервер
     * 2. Pull данных с сервера
     * 
     * В offline режиме ничего не делает.
     */
    override fun sync() {
        if (_isSyncing.value) return
        if (!isOnline.value) return // Offline — пропускаем
        
        scope.launch {
            _isSyncing.value = true
            _lastError.value = null
            
            try {
                withContext(Dispatchers.Default) {
                    pushToServer()
                    pullFromServer()
                }
            } catch (e: Exception) {
                _lastError.value = e.message
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Только pull данных с сервера (без push).
     * Используется при первой загрузке после авторизации.
     * 
     * В offline режиме ничего не делает.
     */
    override fun pullOnly() {
        if (_isSyncing.value) return
        if (!isOnline.value) return // Offline — пропускаем
        
        scope.launch {
            _isSyncing.value = true
            _lastError.value = null
            
            try {
                withContext(Dispatchers.Default) {
                    pullFromServer()
                }
            } catch (e: Exception) {
                _lastError.value = e.message
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Синхронизирует создание нового пива.
     * Вызывается после локального добавления.
     * 
     * В offline режиме данные уже сохранены локально со статусом PENDING_CREATE,
     * синхронизация произойдёт при восстановлении соединения.
     */
    override fun syncCreate(localId: Long) {
        if (!isOnline.value) return // Offline — синхронизируется позже
        
        scope.launch {
            withContext(Dispatchers.Default) {
                val beer = repository.getById(localId) ?: return@withContext
                if (beer.syncStatus != SyncStatus.PENDING_CREATE) return@withContext

                val result = beerApi.addBeer(
                    BeerRequest(
                        id = null,
                        name = beer.name,
                        rating = beer.rating,
                        abv = beer.abv,
                        description = beer.comment
                    )
                )

                result.onSuccess { response ->
                    repository.updateSyncStatus(
                        id = localId,
                        serverId = response.id,
                        status = SyncStatus.SYNCED
                    )
                }
                // При ошибке оставляем PENDING_CREATE — синхронизируется позже
            }
        }
    }

    /**
     * Синхронизирует удаление пива.
     * Вызывается после локальной пометки на удаление.
     * 
     * В offline режиме запись уже помечена как PENDING_DELETE,
     * синхронизация произойдёт при восстановлении соединения.
     */
    override fun syncDelete(localId: Long) {
        if (!isOnline.value) return // Offline — синхронизируется позже
        
        scope.launch {
            withContext(Dispatchers.Default) {
                val beer = repository.getById(localId) ?: return@withContext
                if (beer.syncStatus != SyncStatus.PENDING_DELETE) return@withContext

                val serverId = beer.serverId
                if (serverId == null) {
                    // Нет server_id — просто удаляем локально
                    repository.deletePhysically(localId)
                    return@withContext
                }

                val result = beerApi.deleteBeer(serverId)

                result.onSuccess {
                    repository.deletePhysically(localId)
                }
                // При ошибке оставляем PENDING_DELETE — синхронизируется позже
            }
        }
    }

    /**
     * Синхронизирует все ожидающие изменения.
     * Вызывается при восстановлении соединения.
     */
    private fun syncPendingChanges() {
        if (_isSyncing.value) return
        
        scope.launch {
            _isSyncing.value = true
            _lastError.value = null
            
            try {
                withContext(Dispatchers.Default) {
                    pushToServer()
                }
            } catch (e: Exception) {
                _lastError.value = e.message
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Отправляет локальные изменения на сервер.
     */
    private suspend fun pushToServer() {
        // Отправляем новые записи
        val pendingCreate = repository.getPendingCreate()
        for (beer in pendingCreate) {
            val localId = beer.id ?: continue
            
            val result = beerApi.addBeer(
                BeerRequest(
                    id = null,
                    name = beer.name,
                    rating = beer.rating,
                    abv = beer.abv,
                    description = beer.comment
                )
            )

            result.onSuccess { response ->
                repository.updateSyncStatus(
                    id = localId,
                    serverId = response.id,
                    status = SyncStatus.SYNCED
                )
            }
        }

        // Удаляем помеченные записи
        val pendingDelete = repository.getPendingDelete()
        for (beer in pendingDelete) {
            val localId = beer.id ?: continue
            val serverId = beer.serverId

            if (serverId == null) {
                repository.deletePhysically(localId)
                continue
            }

            val result = beerApi.deleteBeer(serverId)

            result.onSuccess {
                repository.deletePhysically(localId)
            }
        }
    }

    /**
     * Загружает данные с сервера и обновляет локальную БД.
     */
    private suspend fun pullFromServer() {
        val result = beerApi.getBeers()

        result.onSuccess { serverBeers ->
            // Удаляем синхронизированные записи (которые точно есть на сервере)
            // Локальные несинхронизированные записи сохраняем
            repository.deleteSynced()

            // Добавляем данные с сервера
            for (serverBeer in serverBeers) {
                // Проверяем, нет ли уже такой записи (по server_id)
                val existing = repository.getByServerId(serverBeer.id)
                if (existing == null) {
                    repository.insertFromServer(
                        serverId = serverBeer.id,
                        name = serverBeer.name,
                        abv = serverBeer.abv,
                        rating = serverBeer.rating,
                        comment = serverBeer.description
                    )
                }
            }
        }
    }
}
