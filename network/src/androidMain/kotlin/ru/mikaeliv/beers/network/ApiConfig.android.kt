package ru.mikaeliv.beers.network

/**
 * Android эмулятор использует 10.0.2.2 для доступа к localhost хост-машины.
 */
internal actual fun platformBaseUrl(): String = "http://10.0.2.2:8085"
