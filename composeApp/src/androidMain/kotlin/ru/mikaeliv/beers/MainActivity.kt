package ru.mikaeliv.beers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ru.mikaeliv.beers.data.DatabaseDriverFactory
import ru.mikaeliv.beers.di.RepositoryProvider
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import ru.mikaeliv.beers.composeDS.storage.SettingsStorage
import ru.mikaeliv.beers.composeDS.language.LanguageState
import ru.mikaeliv.beers.composeDS.theme.ThemeState
import ru.mikaeliv.beers.network.connectivity.NetworkState
import ru.mikaeliv.beers.network.auth.initTokenStorage
import ru.mikaeliv.beers.network.connectivity.createNetworkMonitor
import ru.mikaeliv.beers.network.connectivity.initNetworkMonitor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Инициализируем хранилище токенов
        initTokenStorage(this)
        
        // Инициализируем мониторинг сети
        initNetworkMonitor(this)
        NetworkState.init(createNetworkMonitor())

        // Инициализируем тему из сохраненных настроек
        val settingsStorage = SettingsStorage(this)
        ThemeState.init(settingsStorage)
        LanguageState.init(settingsStorage)

        RepositoryProvider.provideRepository(DatabaseDriverFactory(this)) { repo ->
            val lifecycle = lifecycle.asEssentyLifecycle()
            val stateKeeper = StateKeeperDispatcher()
            val instanceKeeper = InstanceKeeperDispatcher()
            val backDispatcher = BackDispatcher()
            // Точка входа Android: передаем платформенные Essenty объекты в UI
            setContent { ru.mikaeliv.beers.ui.BeersAppWithContext(repo, lifecycle, stateKeeper, instanceKeeper, backDispatcher) }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {}
