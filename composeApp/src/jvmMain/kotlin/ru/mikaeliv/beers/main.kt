package ru.mikaeliv.beers

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.app_name
import ru.mikaeliv.beers.data.DatabaseDriverFactory
import ru.mikaeliv.beers.di.RepositoryProvider
import ru.mikaeliv.beers.ui.BeersAppWithContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.backhandler.BackDispatcher
import ru.mikaeliv.beers.composeDS.storage.SettingsStorage
import ru.mikaeliv.beers.composeDS.theme.ThemeState
import ru.mikaeliv.beers.network.connectivity.NetworkState
import ru.mikaeliv.beers.network.connectivity.createNetworkMonitor
import org.jetbrains.compose.resources.getString

fun main() {
    // Инициализируем мониторинг сети
    NetworkState.init(createNetworkMonitor())
    
    // Инициализируем тему из сохраненных настроек
    ThemeState.init(SettingsStorage(null))

    application {
        val (windowTitle, setWindowTitle) = remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            setWindowTitle(getString(Res.string.app_name))
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = windowTitle,
        ) {
            val (repo, setRepo) = remember { mutableStateOf<ru.mikaeliv.beers.data.IBeerRepository?>(null) }
            LaunchedEffect(Unit) {
                RepositoryProvider.provideRepository(DatabaseDriverFactory()) { setRepo(it) }
            }
            repo?.let {
                val lifecycle = LifecycleRegistry()
                val stateKeeper = StateKeeperDispatcher()
                val instanceKeeper = InstanceKeeperDispatcher()
                val backDispatcher = BackDispatcher()
                // Точка входа Desktop (JVM): создаем Essenty объекты и передаем в UI
                BeersAppWithContext(it, lifecycle, stateKeeper, instanceKeeper, backDispatcher)
            }
        }
    }
}
