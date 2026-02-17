package ru.mikaeliv.beers.network.connectivity

import kotlinx.coroutines.flow.StateFlow

/**
 * Монитор состояния сетевого соединения.
 * Реализуется платформенно для отслеживания connectivity.
 */
interface NetworkMonitor {
    /** Текущее состояние соединения. */
    val isOnline: StateFlow<Boolean>
    
    /** Запустить мониторинг (вызывается один раз при старте). */
    fun startMonitoring()
    
    /** Остановить мониторинг. */
    fun stopMonitoring()
}

/**
 * Синглтон для глобального доступа к состоянию сети.
 */
object NetworkState {
    private var monitor: NetworkMonitor? = null
    
    /** Инициализация с платформенным монитором. */
    fun init(networkMonitor: NetworkMonitor) {
        monitor = networkMonitor
        networkMonitor.startMonitoring()
    }
    
    /** Текущее состояние соединения. */
    val isOnline: StateFlow<Boolean>
        get() = monitor?.isOnline ?: throw IllegalStateException("NetworkState not initialized")
    
    /** Проверка инициализации. */
    val isInitialized: Boolean
        get() = monitor != null
}
