package ru.mikaeliv.beers.data

import app.cash.sqldelight.db.SqlDriver
// JS-драйвер временно отключен: можно подключить sql.js или web worker драйвер при необходимости
import ru.mikaeliv.beers.db.BeersDatabase

actual class DatabaseDriverFactory actual constructor(private val context: Any?) {
    actual suspend fun createDriver(): SqlDriver = throw UnsupportedOperationException("JS driver not configured")
}

