package ru.mikaeliv.beers.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        })
    }
    install(Logging) {
        level = LogLevel.BODY
    }
    engine {
        config {
            connectTimeout(ApiConfig.connectTimeoutMillis, TimeUnit.MILLISECONDS)
            readTimeout(ApiConfig.requestTimeoutMillis, TimeUnit.MILLISECONDS)
            writeTimeout(ApiConfig.requestTimeoutMillis, TimeUnit.MILLISECONDS)
        }
    }
}
