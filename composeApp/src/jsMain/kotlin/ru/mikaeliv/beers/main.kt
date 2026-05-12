package ru.mikaeliv.beers

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import ru.mikaeliv.beers.composeDS.storage.SettingsStorage
import ru.mikaeliv.beers.composeDS.language.LanguageState
import ru.mikaeliv.beers.composeDS.theme.ThemeState
import ru.mikaeliv.beers.network.connectivity.NetworkState
import ru.mikaeliv.beers.data.DatabaseDriverFactory
import ru.mikaeliv.beers.di.RepositoryProvider
import ru.mikaeliv.beers.network.connectivity.createNetworkMonitor
import ru.mikaeliv.beers.ui.BeersAppWithContext
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Инициализируем мониторинг сети
    NetworkState.init(createNetworkMonitor())
    
    // Инициализируем тему из сохраненных настроек
    val settingsStorage = SettingsStorage(null)
    ThemeState.init(settingsStorage)
    LanguageState.init(settingsStorage)

    ComposeViewport(document.body!!) {
        val (repo, setRepo) = remember { mutableStateOf<ru.mikaeliv.beers.data.IBeerRepository?>(null) }
        LaunchedEffect(Unit) {
            RepositoryProvider.provideRepository(DatabaseDriverFactory()) { setRepo(it) }
        }
        repo?.let {
            val lifecycle = LifecycleRegistry()
            val stateKeeper = StateKeeperDispatcher()
            val instanceKeeper = InstanceKeeperDispatcher()
            val backDispatcher = BackDispatcher()
            // Точка входа Web (JS): создаем Essenty объекты и передаем в UI
            BeersAppWithContext(it, lifecycle, stateKeeper, instanceKeeper, backDispatcher)
        }
    }
}
