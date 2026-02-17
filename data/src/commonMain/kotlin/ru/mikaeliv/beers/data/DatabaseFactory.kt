package ru.mikaeliv.beers.data

import app.cash.sqldelight.db.SqlDriver
import ru.mikaeliv.beers.db.BeersDatabase

/**
 * Платформенная фабрика драйвера SQLDelight.
 * Реализации находятся в androidMain/iosMain/jsMain/jvmMain.
 */
expect class DatabaseDriverFactory(context: Any? = null) {
    suspend fun createDriver(): SqlDriver
}

/**
 * Создает инстанс базы данных, используя предоставленный драйвер.
 */
object DatabaseFactory {
    suspend fun createDatabase(driverFactory: DatabaseDriverFactory): BeersDatabase =
        BeersDatabase(driverFactory.createDriver())
}

