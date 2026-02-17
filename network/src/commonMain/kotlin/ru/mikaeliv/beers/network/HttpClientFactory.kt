package ru.mikaeliv.beers.network

import io.ktor.client.HttpClient

/**
 * Фабрика для создания платформозависимого HttpClient.
 */
expect fun createHttpClient(): HttpClient
