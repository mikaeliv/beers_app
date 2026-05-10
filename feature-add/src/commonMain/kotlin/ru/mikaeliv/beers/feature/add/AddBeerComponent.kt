package ru.mikaeliv.beers.feature.add

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import ru.mikaeliv.beers.core.Beer
import ru.mikaeliv.beers.core.SyncActions
import ru.mikaeliv.beers.data.IBeerRepository

/**
 * Состояние экрана добавления пива.
 */
data class AddBeerState(
    val name: String = "",
    val abv: String = "",
    val rating: Int = 1,
    val comment: String = "",
    val photoBytes: ByteArray? = null,
    val isSaving: Boolean = false,
    val isValid: Boolean = false,
)

interface AddBeerComponent {
    /** Текущее состояние формы добавления. */
    val state: Value<AddBeerState>
    /** Вернуться назад (к списку). */
    fun onBack()
    /** Изменение названия. */
    fun onNameChange(value: String)
    /** Изменение крепости ABV. */
    fun onAbvChange(value: String)
    /** Изменение оценки. */
    fun onRatingChange(value: Int)
    /** Изменение комментария. */
    fun onCommentChange(value: String)
    /** Выбор фотографии. */
    fun onPhotoSelected(bytes: ByteArray)
    /** Попытка сохранить запись. */
    fun onSave()
    /** Освобождение ресурсов. */
    fun onDestroy()

    interface Output {
        /** Уведомление, что запись успешно сохранена. */
        fun saved()
        /** Уведомление, что пользователь нажал назад. */
        fun back()
    }
}

class DefaultAddBeerComponent(
    componentContext: ComponentContext,
    private val repo: IBeerRepository,
    private val syncActions: SyncActions,
    private val output: AddBeerComponent.Output,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val databaseDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AddBeerComponent, ComponentContext by componentContext {
    // Навигация Decompose и обновление UI должны выполняться на main-потоке.
    // Тяжёлые операции БД выполняем через dispatcher, который можно заменить в тестах.
    private val _state = MutableValue(AddBeerState())
    override val state: Value<AddBeerState> = _state

    override fun onBack() {
        output.back()
    }

    override fun onNameChange(value: String) = update { copy(name = value) }
    override fun onAbvChange(value: String) = update { copy(abv = value) }
    override fun onRatingChange(value: Int) = update { copy(rating = value.coerceIn(1, 5)) }
    override fun onCommentChange(value: String) = update { copy(comment = value) }
    override fun onPhotoSelected(bytes: ByteArray) = update { copy(photoBytes = bytes) }

    override fun onSave() {
        val s = _state.value
        val abv = s.abv.toDoubleOrNull() ?: 0.0
        val nameOk = s.name.isNotBlank()
        if (!nameOk || abv <= 0.0 || s.rating !in 1..5 || s.photoBytes == null) return
        _state.value = s.copy(isSaving = true)
        scope.launch {
            // БД — в фоне
            val localId = withContext(databaseDispatcher) {
                repo.add(Beer(null, null, s.name, abv, s.comment.ifBlank { null }, s.rating, s.photoBytes))
            }
            // Запускаем фоновую синхронизацию с сервером
            syncActions.syncCreate(localId)
            // Навигация/Essenty — на main (мы уже на Main.immediate)
            output.saved()
        }
    }

    private inline fun update(block: AddBeerState.() -> AddBeerState) {
        val next = _state.value.block()
        _state.value = next.copy(
            isValid = next.name.isNotBlank() && (next.abv.toDoubleOrNull() ?: 0.0) > 0.0 &&
                next.rating in 1..5 &&
                next.photoBytes != null
        )
    }

    override fun onDestroy() { scope.cancel() }
}
