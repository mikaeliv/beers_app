package ru.mikaeliv.beers.core

/**
 * Статус синхронизации записи с сервером.
 */
enum class SyncStatus {
    /** Запись синхронизирована с сервером. */
    SYNCED,
    
    /** Запись создана локально, ожидает отправки на сервер. */
    PENDING_CREATE,
    
    /** Запись удалена локально, ожидает удаления на сервере. */
    PENDING_DELETE;

    companion object {
        fun fromString(value: String): SyncStatus = when (value) {
            "SYNCED" -> SYNCED
            "PENDING_CREATE" -> PENDING_CREATE
            "PENDING_DELETE" -> PENDING_DELETE
            else -> SYNCED
        }
    }
}
