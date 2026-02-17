package ru.mikaeliv.beers.network

/**
 * Конфигурация API.
 */
object ApiConfig {
    /**
     * Базовый URL API (определяется платформой).
     * Android эмулятор: 10.0.2.2
     * Остальные: localhost
     */
    val baseUrl: String = platformBaseUrl()

    /**
     * Таймаут запроса в миллисекундах.
     */
    var requestTimeoutMillis: Long = 30_000L
        private set

    /**
     * Таймаут подключения в миллисекундах.
     */
    var connectTimeoutMillis: Long = 10_000L
        private set

    /**
     * Инициализация конфигурации API (для переопределения настроек).
     *
     * @param requestTimeoutMillis таймаут запроса (по умолчанию 30 сек)
     * @param connectTimeoutMillis таймаут подключения (по умолчанию 10 сек)
     */
    fun init(
        requestTimeoutMillis: Long = 30_000L,
        connectTimeoutMillis: Long = 10_000L,
    ) {
        this.requestTimeoutMillis = requestTimeoutMillis
        this.connectTimeoutMillis = connectTimeoutMillis
    }
}

/**
 * Возвращает базовый URL API для текущей платформы.
 */
internal expect fun platformBaseUrl(): String
