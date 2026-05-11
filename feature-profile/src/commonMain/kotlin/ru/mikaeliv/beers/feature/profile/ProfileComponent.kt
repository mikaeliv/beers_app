package ru.mikaeliv.beers.feature.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import ru.mikaeliv.beers.network.api.IAuthApi

/**
 * Состояние экрана профиля.
 */
data class ProfileState(
    val email: String = "",
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
    private val authApi: IAuthApi,
    private val output: ProfileComponent.Output,
) : ProfileComponent, ComponentContext by componentContext {

    private val _state = MutableValue(ProfileState(email = email))
    override val state: Value<ProfileState> = _state

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
}
