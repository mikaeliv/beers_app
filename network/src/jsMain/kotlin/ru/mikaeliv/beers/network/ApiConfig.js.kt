package ru.mikaeliv.beers.network

/**
 * Web (JS) использует localhost напрямую.
 */
internal actual fun platformBaseUrl(): String = "http://localhost:8085"
