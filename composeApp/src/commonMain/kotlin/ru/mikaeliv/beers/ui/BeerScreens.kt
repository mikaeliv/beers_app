package ru.mikaeliv.beers.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import ru.mikaeliv.beers.data.IBeerRepository
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import ru.mikaeliv.beers.composeDS.theme.BeersTheme
import ru.mikaeliv.beers.composeDS.language.LanguageState
import ru.mikaeliv.beers.sync.SyncEngine

/**
 * Платформенная точка входа UI.
 * Использует переданные Essenty объекты для корректной навигации/сохранения состояния.
 */
@Composable
fun BeersAppWithContext(
    repo: IBeerRepository,
    lifecycle: com.arkivanov.essenty.lifecycle.Lifecycle,
    stateKeeper: com.arkivanov.essenty.statekeeper.StateKeeper,
    instanceKeeper: com.arkivanov.essenty.instancekeeper.InstanceKeeper,
    backHandler: BackHandler,
) {
    val syncEngine = remember { SyncEngine(repo) }
    val root = rememberRootComponent(repo, syncEngine, lifecycle, stateKeeper, instanceKeeper, backHandler)
    val appLanguage = LanguageState.language
    
    key(appLanguage) {
        BeersTheme {
            RootUi(root)
        }
    }
}
