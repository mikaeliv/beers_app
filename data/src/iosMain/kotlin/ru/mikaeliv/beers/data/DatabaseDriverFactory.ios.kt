package ru.mikaeliv.beers.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import ru.mikaeliv.beers.db.BeersDatabase

actual class DatabaseDriverFactory actual constructor(private val context: Any?) {
    actual suspend fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = BeersDatabase.Schema,
            name = "beers.db"
        )
    }
}
