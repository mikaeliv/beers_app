package ru.mikaeliv.beers.feature.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.mikaeliv.beers.data.IBeerRepository
import ru.mikaeliv.beers.network.api.IAuthApi

/**
 * Состояние экрана профиля.
 */
data class ProfileState(
    val email: String = "",
    val beerCount: Int = 0,
    val averageRating: Double = 0.0,
)

interface ProfileComponent {
    val state: Value<ProfileState>

    fun onBack()
    fun onSettingsClick()
    fun onLogout()

    interface Output {
        fun back()
        fun openSettings()
        fun onLogout()
    }
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val email: String,
    private val repo: IBeerRepository,
    private val authApi: IAuthApi,
    private val output: ProfileComponent.Output,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : ProfileComponent, ComponentContext by componentContext {

    private val _state = MutableValue(ProfileState(email = email))
    override val state: Value<ProfileState> = _state

    init {
        scope.launch {
            repo.getAll().collect { beers ->
                _state.value = _state.value.copy(
                    beerCount = beers.size,
                    averageRating = beers.map { it.rating }.average().takeIf { !it.isNaN() } ?: 0.0
                )
            }
        }
    }

    override fun onBack() {
        output.back()
    }

    override fun onSettingsClick() {
        output.openSettings()
    }

    override fun onLogout() {
        authApi.logout()
        output.onLogout()
    }

    fun onDestroy() {
        scope.cancel()
    }
}
