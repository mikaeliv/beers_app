package ru.mikaeliv.beers.feature.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import ru.mikaeliv.beers.composeDS.theme.ThemeState

data class SettingsState(
    val isDarkTheme: Boolean = ThemeState.isDarkTheme
)

interface SettingsComponent {
    val state: Value<SettingsState>
    fun onBack()
    fun onDarkThemeToggle(enabled: Boolean)

    interface Output {
        fun back()
    }
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val output: SettingsComponent.Output,
) : SettingsComponent, ComponentContext by componentContext {

    private val _state = MutableValue(SettingsState())
    override val state: Value<SettingsState> = _state

    override fun onBack() = output.back()

    override fun onDarkThemeToggle(enabled: Boolean) {
        ThemeState.toggleDarkTheme(enabled)
        _state.value = _state.value.copy(isDarkTheme = enabled)
    }
}
