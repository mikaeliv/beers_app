package ru.mikaeliv.beers.feature.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mikaeliv.beers.core.Beer
import ru.mikaeliv.beers.core.SyncActions
import ru.mikaeliv.beers.data.IBeerRepository

data class BeerDetailState(
    val isLoading: Boolean = true,
    val beer: Beer? = null,
)

interface BeerDetailComponent {
    val state: Value<BeerDetailState>
    fun onBack()
    fun onDelete()
    fun onDestroy()

    interface Output {
        fun back()
    }
}

class DefaultBeerDetailComponent(
    componentContext: ComponentContext,
    private val repo: IBeerRepository,
    private val syncActions: SyncActions,
    private val beerId: Long,
    private val output: BeerDetailComponent.Output,
) : BeerDetailComponent, ComponentContext by componentContext {

    // Навигация Decompose и обновление UI — на main.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableValue(BeerDetailState())
    override val state: Value<BeerDetailState> = _state

    init {
        scope.launch {
            val beer = withContext(Dispatchers.Default) { repo.getById(beerId) }
            _state.value = BeerDetailState(isLoading = false, beer = beer)
        }
    }

    override fun onBack() = output.back()

    override fun onDelete() {
        val id = _state.value.beer?.id ?: return
        scope.launch {
            withContext(Dispatchers.Default) { repo.delete(id) }
            // Запускаем фоновую синхронизацию удаления с сервером
            syncActions.syncDelete(id)
            output.back()
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
