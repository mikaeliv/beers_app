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
import ru.mikaeliv.beers.network.ApiConfig
import java.net.URI
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
            val uri = URI(ApiConfig.baseUrl)
            val host = uri.host ?: return true
            val port = when {
                uri.port != -1 -> uri.port
                uri.scheme == "https" -> 443
                else -> 80
            }
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 1500)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
