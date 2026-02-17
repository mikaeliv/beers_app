package ru.mikaeliv.beers.network

/**
 * iOS симулятор использует localhost напрямую.
 */
internal actual fun platformBaseUrl(): String = "http://localhost:8085"
