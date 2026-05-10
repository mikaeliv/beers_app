package ru.mikaeliv.beers.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import ru.mikaeliv.beers.network.ApiResult
import ru.mikaeliv.beers.network.connectivity.NetworkState
import ru.mikaeliv.beers.core.SyncActions
import ru.mikaeliv.beers.core.SyncStatus
import ru.mikaeliv.beers.data.IBeerRepository
import ru.mikaeliv.beers.network.api.BeerApi
import ru.mikaeliv.beers.network.dto.BeerRequest

/**
 * Движок синхронизации данных между локальной БД и сервером.
 * Реализует offline-first архитектуру с автоматической синхронизацией
 * при восстановлении соединения.
 */
private const val PAGE_SIZE = 20

class SyncEngine(
    private val repository: IBeerRepository,
    private val beerApi: BeerApi = BeerApi(),
) : SyncActions {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _hasMorePages = MutableStateFlow(true)
    override val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    override val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val syncMutex = Mutex()
    private var lastLoadedPage = -1
    
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
     * Если синхронизация уже запущена, повторный вызов пропускается.
     */
    override fun sync() {
        if (!isOnline.value) return // Offline — пропускаем
        
        scope.launch {
            if (!syncMutex.tryLock()) return@launch
            try {
                _isSyncing.value = true
                _lastError.value = null

                try {
                    withContext(Dispatchers.Default) {
                        pushToServer()
                        pullFromServer(fetchAllPages = true)
                    }
                } catch (e: Exception) {
                    _lastError.value = e.message
                } finally {
                    _isSyncing.value = false
                }
            } finally {
                syncMutex.unlock()
            }
        }
    }

    /**
     * Только pull данных с сервера (без push).
     * Используется при первой загрузке после авторизации.
     * Загружает только первую страницу для быстрого отображения.
     * 
     * В offline режиме ничего не делает.
     * Если синхронизация уже запущена, повторный вызов пропускается.
     */
    override fun pullOnly() {
        if (!isOnline.value) return // Offline — пропускаем
        
        scope.launch {
            if (!syncMutex.tryLock()) return@launch
            try {
                _isSyncing.value = true
                _lastError.value = null

                try {
                    withContext(Dispatchers.Default) {
                        pullFromServer(fetchAllPages = false)
                    }
                } catch (e: Exception) {
                    _lastError.value = e.message
                } finally {
                    _isSyncing.value = false
                }
            } finally {
                syncMutex.unlock()
            }
        }
    }

    /**
     * Загружает следующую страницу (для infinite scroll).
     */
    override fun loadMore() {
        if (_isLoadingMore.value) return
        if (!_hasMorePages.value) return
        if (!isOnline.value) return
        
        scope.launch {
            _isLoadingMore.value = true
            _lastError.value = null
            
            try {
                withContext(Dispatchers.Default) {
                    val nextPage = lastLoadedPage + 1
                    val result = beerApi.getBeers(page = nextPage, size = PAGE_SIZE)
                    
                    result.onSuccess { pageResponse ->
                        for (serverBeer in pageResponse.content) {
                            val existing = repository.getByServerId(serverBeer.id)
                            if (existing == null) {
                                val photoBytes = loadPhoto(serverBeer.imageUrl)
                                repository.insertFromServer(
                                    serverId = serverBeer.id,
                                    name = serverBeer.name,
                                    abv = serverBeer.abv,
                                    rating = serverBeer.rating,
                                    comment = serverBeer.description,
                                    photoBytes = photoBytes
                                )
                            }
                        }
                        lastLoadedPage = nextPage
                        _hasMorePages.value = (pageResponse.page.number + 1) < pageResponse.page.totalPages
                    }
                }
            } catch (e: Exception) {
                _lastError.value = e.message
            } finally {
                _isLoadingMore.value = false
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

                val photoBytes = beer.photoBytes ?: run {
                    _lastError.value = "Image is required"
                    return@withContext
                }
                val result = beerApi.addBeer(
                    request = BeerRequest(
                        id = null,
                        name = beer.name,
                        rating = beer.rating,
                        abv = beer.abv,
                        description = beer.comment
                    ),
                    imageBytes = photoBytes
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
     *
     * Не запускается параллельно с ручной синхронизацией или первичным pull.
     */
    private fun syncPendingChanges() {
        scope.launch {
            if (!syncMutex.tryLock()) return@launch
            try {
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
            } finally {
                syncMutex.unlock()
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
            
            val photoBytes = beer.photoBytes
            if (photoBytes == null) {
                _lastError.value = "Image is required"
                continue
            }
            val result = beerApi.addBeer(
                request = BeerRequest(
                    id = null,
                    name = beer.name,
                    rating = beer.rating,
                    abv = beer.abv,
                    description = beer.comment
                ),
                imageBytes = photoBytes
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
     * @param fetchAllPages если true — загружает все страницы; если false — только первую
     */
    private suspend fun pullFromServer(fetchAllPages: Boolean = true) {
        // Удаляем синхронизированные записи
        repository.deleteSynced()
        
        lastLoadedPage = -1
        var page = 0
        var hasMore = true
        
        while (hasMore) {
            val result = beerApi.getBeers(page = page, size = PAGE_SIZE)
            
            when (result) {
                is ApiResult.Success -> {
                    val pageResponse = result.data
                    val serverBeers = pageResponse.content
                    for (serverBeer in serverBeers) {
                        val existing = repository.getByServerId(serverBeer.id)
                        if (existing == null) {
                            val photoBytes = loadPhoto(serverBeer.imageUrl)
                            repository.insertFromServer(
                                serverId = serverBeer.id,
                                name = serverBeer.name,
                                abv = serverBeer.abv,
                                rating = serverBeer.rating,
                                comment = serverBeer.description,
                                photoBytes = photoBytes
                            )
                        }
                    }
                    lastLoadedPage = page
                    val hasNextPage = (pageResponse.page.number + 1) < pageResponse.page.totalPages
                    _hasMorePages.value = hasNextPage
                    hasMore = fetchAllPages && hasNextPage
                    page++
                }
                is ApiResult.Error -> {
                    _lastError.value = result.message
                    break
                }
            }
        }
    }

    private suspend fun loadPhoto(imageUrl: String): ByteArray? {
        return when (val result = beerApi.getBeerImage(imageUrl)) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> {
                _lastError.value = result.message
                null
            }
        }
    }
}
