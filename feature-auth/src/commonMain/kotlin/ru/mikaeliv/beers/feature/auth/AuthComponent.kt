package ru.mikaeliv.beers.feature.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mikaeliv.beers.network.api.AuthApi

/**
 * Состояние экрана авторизации.
 */
data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginMode: Boolean = true, // true = вход, false = регистрация
)

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
    private val authApi: AuthApi,
    private val output: AuthComponent.Output,
) : AuthComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
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
            _state.value = s.copy(error = "Заполните все поля")
            return
        }

        _state.value = s.copy(isLoading = true, error = null)

        scope.launch {
            val result = withContext(Dispatchers.Default) {
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
                        error = error.message
                    )
                }
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
