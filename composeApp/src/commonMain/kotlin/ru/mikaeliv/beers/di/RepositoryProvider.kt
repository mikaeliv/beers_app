package ru.mikaeliv.beers.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.mikaeliv.beers.data.IBeerRepository
import ru.mikaeliv.beers.data.DatabaseDriverFactory
import ru.mikaeliv.beers.data.DatabaseFactory
import ru.mikaeliv.beers.data.BeerRepositoryImpl

object RepositoryProvider {
    private var repository: IBeerRepository? = null

    fun provideRepository(driverFactory: DatabaseDriverFactory, onReady: (IBeerRepository) -> Unit) {
        val existing = repository
        if (existing != null) {
            onReady(existing)
            return
        }
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            val db = DatabaseFactory.createDatabase(driverFactory)
            val repo = BeerRepositoryImpl(db)
            repository = repo
            onReady(repo)
        }
    }
}

