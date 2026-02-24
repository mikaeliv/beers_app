package ru.mikaeliv.beers.core

import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для действий синхронизации.
 * Позволяет feature-модулям запускать sync без прямой зависимости от SyncEngine.
 */
interface SyncActions {
    /** Статус синхронизации. */
    val isSyncing: StateFlow<Boolean>
    
    /** Есть ли ещё страницы для загрузки. */
    val hasMorePages: StateFlow<Boolean>
    
    /** Статус загрузки следующей страницы. */
    val isLoadingMore: StateFlow<Boolean>
    
    /** Полная синхронизация (push + pull). */
    fun sync()
    
    /** Только загрузка с сервера. */
    fun pullOnly()
    
    /** Загрузить следующую страницу (для infinite scroll). */
    fun loadMore()
    
    /** Синхронизация после создания записи. */
    fun syncCreate(localId: Long)
    
    /** Синхронизация после удаления записи. */
    fun syncDelete(localId: Long)
}
