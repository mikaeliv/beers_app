package ru.mikaeliv.beers.network.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

actual fun createNetworkMonitor(): NetworkMonitor = JvmNetworkMonitor()

private class JvmNetworkMonitor : NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private var monitorJob: Job? = null
    
    override fun startMonitoring() {
        monitorJob = scope.launch {
            while (isActive) {
                _isOnline.value = checkConnectivity()
                delay(5000) // Проверяем каждые 5 секунд
            }
        }
    }
    
    override fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }
    
    private fun checkConnectivity(): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
