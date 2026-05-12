package ru.mikaeliv.beers.feature.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.mikaeliv.beers.core.Beer
import ru.mikaeliv.beers.core.SyncActions
import ru.mikaeliv.beers.data.IBeerRepository

private const val EMPTY_LOAD_MORE_BEER_COUNT = 0

interface BeerListComponent {
    /** Состояние: текущий список пив. */
    val state: Value<List<Beer>>
    /** Статус синхронизации. */
    val isRefreshing: StateFlow<Boolean>
    /** Есть ли ещё страницы для загрузки. */
    val hasMorePages: StateFlow<Boolean>
    /** Статус загрузки следующей страницы. */
    val isLoadingMore: StateFlow<Boolean>
    /** Обработчик клика по кнопке добавления. */
    fun onAddClick()
    /** Открыть детальный экран по id. */
    fun onOpen(beerId: Long)
    /** Открыть профиль. */
    fun onProfileClick()
    /** Обновить данные (pull-to-refresh). */
    fun onRefresh()
    /** Загрузить следующую страницу. */
    fun onLoadMore()
    /** Освободить ресурсы (отмена корутин и т.п.). */
    fun onDestroy()

    interface Output {
        /** Сообщает роутеру: открыть экран добавления. */
        fun openAdd()
        /** Сообщает роутеру: открыть экран деталей. */
        fun openDetail(id: Long)
        /** Сообщает роутеру: открыть профиль. */
        fun openProfile()
    }
}

class DefaultBeerListComponent(
    componentContext: ComponentContext,
    private val repo: IBeerRepository,
    private val syncEngine: SyncActions,
    private val output: BeerListComponent.Output,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : BeerListComponent, ComponentContext by componentContext {

    // Навигация и обновление UI — на main.
    private val _state = MutableValue<List<Beer>>(emptyList())
    override val state: Value<List<Beer>> = _state
    override val isRefreshing: StateFlow<Boolean> = syncEngine.isSyncing
    override val hasMorePages: StateFlow<Boolean> = syncEngine.hasMorePages
    override val isLoadingMore: StateFlow<Boolean> = syncEngine.isLoadingMore
    private var lastLoadMoreBeerCount = -1

    init {
        scope.launch {
            // Поток SQLDelight сам выберет подходящий диспетчер; здесь остаёмся на main,
            // так как коллекция возвращает уже готовые объекты.
            repo.getAll().collect { _state.value = it }
        }
        syncEngine.pullOnly()
    }

    override fun onAddClick() = output.openAdd()
    override fun onOpen(beerId: Long) = output.openDetail(beerId)
    override fun onProfileClick() = output.openProfile()
    override fun onRefresh() {
        lastLoadMoreBeerCount = -1
        syncEngine.sync()
    }
    override fun onLoadMore() {
        val beerCount = state.value.size
        if (beerCount == EMPTY_LOAD_MORE_BEER_COUNT) return
        if (!hasMorePages.value) return
        if (isLoadingMore.value) return
        if (beerCount == lastLoadMoreBeerCount) return

        lastLoadMoreBeerCount = beerCount
        syncEngine.loadMore()
    }
    override fun onDestroy() { scope.cancel() }
}
