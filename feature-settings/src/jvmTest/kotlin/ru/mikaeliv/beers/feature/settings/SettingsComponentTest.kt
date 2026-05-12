package ru.mikaeliv.beers.feature.settings

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import ru.mikaeliv.beers.composeDS.language.AppLanguage
import ru.mikaeliv.beers.composeDS.language.LanguageState
import ru.mikaeliv.beers.composeDS.theme.ThemeState
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsComponentTest {

    /**
     * Возвращает глобальное состояние темы в нейтральное значение после каждого теста.
     */
    @AfterTest
    fun tearDown() {
        ThemeState.toggleDarkTheme(false)
        LanguageState.selectLanguage(AppLanguage.English)
    }

    /**
     * Проверяет, что начальное состояние компонента читает текущий ThemeState.
     */
    @Test
    fun initialStateReadsThemeState() {
        ThemeState.toggleDarkTheme(true)

        val fixture = createFixture()

        assertEquals(true, fixture.component.state.value.isDarkTheme)
        assertEquals(AppLanguage.English, fixture.component.state.value.language)
    }

    /**
     * Проверяет, что переключение темы обновляет и глобальный ThemeState, и state компонента.
     */
    @Test
    fun onDarkThemeToggleUpdatesThemeStateAndComponentState() {
        val fixture = createFixture()

        fixture.component.onDarkThemeToggle(true)

        assertEquals(true, ThemeState.isDarkTheme)
        assertEquals(true, fixture.component.state.value.isDarkTheme)
    }

    /**
     * Проверяет, что переключение языка обновляет и глобальный LanguageState, и state компонента.
     */
    @Test
    fun onLanguageChangeUpdatesLanguageStateAndComponentState() {
        val fixture = createFixture()

        fixture.component.onLanguageChange(AppLanguage.Russian)

        assertEquals(AppLanguage.Russian, LanguageState.language)
        assertEquals(AppLanguage.Russian, fixture.component.state.value.language)
    }

    /**
     * Проверяет, что back пробрасывается наружу через output.
     */
    @Test
    fun onBackCallsOutputBack() {
        val fixture = createFixture()

        fixture.component.onBack()

        assertEquals(1, fixture.output.backCalls)
    }

    private fun createFixture(): Fixture {
        val output = FakeOutput()
        val component = DefaultSettingsComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            output = output
        )
        return Fixture(component, output)
    }

    private data class Fixture(
        val component: SettingsComponent,
        val output: FakeOutput,
    )

    private class FakeOutput : SettingsComponent.Output {
        var backCalls = 0
        override fun back() { backCalls += 1 }
    }
}
