package ru.mikaeliv.beers

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
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

private var initialized = false

fun MainViewController() = ComposeUIViewController {
    // Инициализация один раз
    LaunchedEffect(Unit) {
        if (!initialized) {
            initialized = true
            NetworkState.init(createNetworkMonitor())
            ThemeState.init(SettingsStorage(null))
        }
    }

    val repo = androidx.compose.runtime.produceState<ru.mikaeliv.beers.data.BeerRepository?>(initialValue = null) {
        RepositoryProvider.provideRepository(DatabaseDriverFactory()) { value = it }
    }.value
    if (repo != null) {
        val lifecycle = LifecycleRegistry()
        val stateKeeper = StateKeeperDispatcher()
        val instanceKeeper = InstanceKeeperDispatcher()
        val backDispatcher = BackDispatcher()
        // Точка входа iOS: передаем Essenty объекты в общий UI
        BeersAppWithContext(repo, lifecycle, stateKeeper, instanceKeeper, backDispatcher)
    }
}
