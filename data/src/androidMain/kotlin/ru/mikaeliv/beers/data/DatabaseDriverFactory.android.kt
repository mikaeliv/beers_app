package ru.mikaeliv.beers.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ru.mikaeliv.beers.db.BeersDatabase

actual class DatabaseDriverFactory actual constructor(private val context: Any?) {
    actual suspend fun createDriver(): SqlDriver {
        val androidContext = context as? Context
            ?: throw IllegalArgumentException("Android context is required")
        return AndroidSqliteDriver(
            schema = BeersDatabase.Schema,
            context = androidContext,
            name = "beers.db"
        )
    }
}
