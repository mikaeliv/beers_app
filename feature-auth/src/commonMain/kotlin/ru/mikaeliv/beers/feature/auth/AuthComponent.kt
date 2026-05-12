package ru.mikaeliv.beers.feature.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mikaeliv.beers.network.api.IAuthApi

/**
 * Состояние экрана авторизации.
 */
data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null,
    val isLoginMode: Boolean = true, // true = вход, false = регистрация
)

sealed interface AuthError {
    data object FillFields : AuthError
    data class Message(val value: String) : AuthError
}

interface AuthComponent {
    val state: Value<AuthState>

    fun onEmailChange(value: String)
    fun onPasswordChange(value: String)
    fun onSubmit()
    fun switchMode()
    fun onDestroy()

    interface Output {
        fun onAuthSuccess()
    }
}

class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val authApi: IAuthApi,
    private val output: AuthComponent.Output,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val workDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AuthComponent, ComponentContext by componentContext {

    private val _state = MutableValue(AuthState())
    override val state: Value<AuthState> = _state

    override fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    override fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    override fun switchMode() {
        _state.value = _state.value.copy(
            isLoginMode = !_state.value.isLoginMode,
            error = null
        )
    }

    override fun onSubmit() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = AuthError.FillFields)
            return
        }

        _state.value = s.copy(isLoading = true, error = null)

        scope.launch {
            val result = withContext(workDispatcher) {
                if (s.isLoginMode) {
                    authApi.login(s.email, s.password)
                } else {
                    authApi.register(s.email, s.password)
                }
            }

            result
                .onSuccess {
                    output.onAuthSuccess()
                }
                .onError { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = AuthError.Message(error.message)
                    )
                }
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
