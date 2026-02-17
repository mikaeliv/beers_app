package ru.mikaeliv.beers.network

/**
 * Результат API-запроса.
 */
sealed class ApiResult<out T> {
    /**
     * Успешный результат.
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * Ошибка запроса.
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val cause: Throwable? = null,
    ) : ApiResult<Nothing>()

    /**
     * Проверяет, является ли результат успешным.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Проверяет, является ли результат ошибкой.
     */
    val isError: Boolean get() = this is Error

    /**
     * Возвращает данные или null, если результат — ошибка.
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Возвращает данные или выбрасывает исключение.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw ApiException(message, code, cause)
    }

    /**
     * Преобразует данные успешного результата.
     */
    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * Выполняет действие при успешном результате.
     */
    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Выполняет действие при ошибке.
     */
    inline fun onError(action: (Error) -> Unit): ApiResult<T> {
        if (this is Error) action(this)
        return this
    }
}

/**
 * Исключение API.
 */
class ApiException(
    override val message: String,
    val code: Int? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
