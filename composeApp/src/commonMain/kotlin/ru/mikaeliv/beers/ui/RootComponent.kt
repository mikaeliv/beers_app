package ru.mikaeliv.beers.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.serialization.Serializable
import ru.mikaeliv.beers.core.SyncActions
import ru.mikaeliv.beers.data.IBeerRepository
import ru.mikaeliv.beers.feature.add.AddBeerComponent
import ru.mikaeliv.beers.feature.add.AddBeerScreen
import ru.mikaeliv.beers.feature.add.DefaultAddBeerComponent
import ru.mikaeliv.beers.feature.auth.AuthComponent
import ru.mikaeliv.beers.feature.auth.AuthScreen
import ru.mikaeliv.beers.feature.auth.DefaultAuthComponent
import ru.mikaeliv.beers.feature.detail.BeerDetailComponent
import ru.mikaeliv.beers.feature.detail.BeerDetailScreen
import ru.mikaeliv.beers.feature.detail.DefaultBeerDetailComponent
import ru.mikaeliv.beers.feature.list.BeerListComponent
import ru.mikaeliv.beers.feature.list.BeerListScreen
import ru.mikaeliv.beers.feature.list.DefaultBeerListComponent
import ru.mikaeliv.beers.feature.profile.DefaultProfileComponent
import ru.mikaeliv.beers.feature.profile.ProfileComponent
import ru.mikaeliv.beers.feature.profile.ProfileScreen
import ru.mikaeliv.beers.feature.settings.DefaultSettingsComponent
import ru.mikaeliv.beers.feature.settings.SettingsComponent
import ru.mikaeliv.beers.feature.settings.SettingsScreen
import ru.mikaeliv.beers.composeDS.OfflineBanner
import ru.mikaeliv.beers.network.connectivity.NetworkState
import ru.mikaeliv.beers.network.api.AuthApi
import ru.mikaeliv.beers.network.api.IAuthApi
import ru.mikaeliv.beers.network.auth.DefaultTokenStorage
import ru.mikaeliv.beers.network.auth.ITokenStorage
import ru.mikaeliv.beers.sync.SyncEngine

/**
 * Корневой компонент приложения.
 * Управляет стек-навигацией между экранами (авторизация, список, детали, добавление).
 */
interface RootComponent {
    val stack: Value<ChildStack<Config, Child>>

    /**
     * Обертки для дочерних компонентов, которые отображаются в UI.
     */
    sealed class Child {
        data class Auth(val component: AuthComponent) : Child()
        data class List(val component: BeerListComponent) : Child()
        data class Detail(val component: BeerDetailComponent) : Child()
        data class Add(val component: AddBeerComponent) : Child()
        data class Profile(val component: ProfileComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
    }
}

@Serializable
sealed class Config {
    @Serializable
    data object Auth : Config()
    @Serializable
    data object List : Config()
    @Serializable
    data class Detail(val id: Long) : Config()
    @Serializable
    data object Add : Config()
    @Serializable
    data object Profile : Config()
    @Serializable
    data object Settings : Config()
}

/**
 * Реализация корневого компонента на Decompose.
 * @param repo общий репозиторий данных
 * @param authApi API авторизации
 * @param tokenStorage хранилище токенов
 * @param syncEngine движок синхронизации
 */
@OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class)
class DefaultRootComponent(
    componentContext: ComponentContext,
    private val repo: IBeerRepository,
    private val authApi: IAuthApi = AuthApi(),
    private val tokenStorage: ITokenStorage = DefaultTokenStorage(),
    private val syncEngine: SyncActions,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    // Определяем начальный экран и запускаем sync если нужно
    private val initialConfig: Config = if (tokenStorage.isLoggedIn()) {
        Config.List
    } else {
        Config.Auth
    }

    override val stack: Value<ChildStack<Config, RootComponent.Child>> = childStack(
        source = navigation,
        initialConfiguration = initialConfig,
        handleBackButton = true,
        childFactory = ::createChild,
        serializer = Config.serializer()
    )

    /**
     * Создает дочерний компонент для заданной конфигурации.
     */
    private fun createChild(config: Config, ctx: ComponentContext): RootComponent.Child = when (config) {
        Config.Auth -> RootComponent.Child.Auth(
            DefaultAuthComponent(ctx, authApi, object : AuthComponent.Output {
                override fun onAuthSuccess() {
                    navigation.replaceAll(Config.List)
                }
            })
        )
        Config.List -> RootComponent.Child.List(
            DefaultBeerListComponent(
                componentContext = ctx,
                repo = repo,
                syncEngine = syncEngine,
                output = object : BeerListComponent.Output {
                    override fun openAdd() { navigation.push(Config.Add) }
                    override fun openDetail(id: Long) { navigation.push(Config.Detail(id)) }
                    override fun openProfile() { navigation.push(Config.Profile) }
                }
            )
        )
        is Config.Detail -> RootComponent.Child.Detail(
            DefaultBeerDetailComponent(ctx, repo, syncEngine, config.id, object : BeerDetailComponent.Output {
                override fun back() { navigation.pop() }
            })
        )
        Config.Add -> RootComponent.Child.Add(
            DefaultAddBeerComponent(ctx, repo, syncEngine, object : AddBeerComponent.Output {
                override fun saved() { navigation.pop() }
                override fun back() { navigation.pop() }
            })
        )
        Config.Profile -> RootComponent.Child.Profile(
            DefaultProfileComponent(
                ctx,
                email = tokenStorage.getEmail() ?: "",
                authApi = authApi,
                output = object : ProfileComponent.Output {
                    override fun back() { navigation.pop() }
                    override fun openSettings() { navigation.push(Config.Settings) }
                    override fun onLogout() {
                        // После выхода заменяем весь стек на экран авторизации
                        navigation.replaceAll(Config.Auth)
                    }
                }
            )
        )
        Config.Settings -> RootComponent.Child.Settings(
            DefaultSettingsComponent(ctx, object : SettingsComponent.Output {
                override fun back() { navigation.pop() }
            })
        )
    }
}

/**
 * Корневой UI: подписывается на стек и отображает активный экран.
 * Показывает баннер offline режима поверх всего контента.
 * Управляет системными отступами (status bar) на уровне приложения.
 */
@Composable
fun RootUi(root: RootComponent) {
    val isOffline by if (NetworkState.isInitialized) {
        NetworkState.isOnline.collectAsState()
    } else {
        androidx.compose.runtime.mutableStateOf(true)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Основной контент - Scaffold сам обработает системные отступы
        Children(stack = root.stack) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.Auth -> AuthScreen(instance.component)
                is RootComponent.Child.List -> BeerListScreen(instance.component)
                is RootComponent.Child.Detail -> BeerDetailScreen(instance.component)
                is RootComponent.Child.Add -> AddBeerScreen(instance.component)
                is RootComponent.Child.Profile -> ProfileScreen(instance.component)
                is RootComponent.Child.Settings -> SettingsScreen(instance.component)
            }
        }
        
        // Баннер offline режима
        OfflineBanner(
            isOffline = !isOffline,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.navigationBars.asPaddingValues())
        )
    }
}

/**
 * Создает и запоминает корневой компонент с переданными Essenty-хендлерами.
 */
@OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class)
@Composable
fun rememberRootComponent(
    repo: IBeerRepository,
    syncEngine: SyncEngine,
    lifecycle: Lifecycle,
    stateKeeper: StateKeeper,
    instanceKeeper: InstanceKeeper,
    backHandler: BackHandler,
): RootComponent {
    return remember(repo, syncEngine, lifecycle, stateKeeper, instanceKeeper, backHandler) {
        val context = DefaultComponentContext(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            instanceKeeper = instanceKeeper,
            backHandler = backHandler,
        )
        DefaultRootComponent(context, repo, syncEngine = syncEngine)
    }
}
