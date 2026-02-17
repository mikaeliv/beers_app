package ru.mikaeliv.beers.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.mikaeliv.beers.data.BeerRepository
import ru.mikaeliv.beers.data.DatabaseDriverFactory
import ru.mikaeliv.beers.data.DatabaseFactory

object RepositoryProvider {
    private var repository: BeerRepository? = null

    fun provideRepository(driverFactory: DatabaseDriverFactory, onReady: (BeerRepository) -> Unit) {
        val existing = repository
        if (existing != null) {
            onReady(existing)
            return
        }
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            val db = DatabaseFactory.createDatabase(driverFactory)
            val repo = BeerRepository(db)
            repository = repo
            onReady(repo)
        }
    }
}


