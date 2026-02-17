package ru.mikaeliv.beers.network.connectivity

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.events.Event
actual fun createNetworkMonitor(): NetworkMonitor = JsNetworkMonitor()

private class JsNetworkMonitor : NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private var onlineHandler: ((Event) -> Unit)? = null
    private var offlineHandler: ((Event) -> Unit)? = null
    
    override fun startMonitoring() {
        // Проверяем текущее состояние
        _isOnline.value = window.navigator.onLine
        
        // Подписываемся на события
        onlineHandler = { _isOnline.value = true }
        offlineHandler = { _isOnline.value = false }
        
        window.addEventListener("online", onlineHandler!!)
        window.addEventListener("offline", offlineHandler!!)
    }
    
    override fun stopMonitoring() {
        onlineHandler?.let { window.removeEventListener("online", it) }
        offlineHandler?.let { window.removeEventListener("offline", it) }
        onlineHandler = null
        offlineHandler = null
    }
}
