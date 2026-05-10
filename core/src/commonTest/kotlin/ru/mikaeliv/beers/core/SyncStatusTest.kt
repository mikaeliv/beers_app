package ru.mikaeliv.beers.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Проверяет преобразование строкового статуса из БД/сети в доменный SyncStatus.
 */
class SyncStatusTest {

    /**
     * Проверяет, что строка SYNCED из БД или API корректно превращается в доменный статус SYNCED.
     */
    @Test
    fun fromStringReturnsSyncedForSyncedValue() {
        // SYNCED хранится строкой в БД и должен без потерь восстанавливаться в enum.
        assertEquals(SyncStatus.SYNCED, SyncStatus.fromString("SYNCED"))
    }

    /**
     * Проверяет, что строка PENDING_CREATE сохраняет смысл локальной записи, ожидающей отправки.
     */
    @Test
    fun fromStringReturnsPendingCreateForPendingCreateValue() {
        // PENDING_CREATE нужен для очереди локальных созданий, ожидающих синхронизации.
        assertEquals(SyncStatus.PENDING_CREATE, SyncStatus.fromString("PENDING_CREATE"))
    }

    /**
     * Проверяет, что строка PENDING_DELETE сохраняет смысл локального удаления, ожидающего синхронизации.
     */
    @Test
    fun fromStringReturnsPendingDeleteForPendingDeleteValue() {
        // PENDING_DELETE нужен для очереди удалений, которые еще надо отправить на сервер.
        assertEquals(SyncStatus.PENDING_DELETE, SyncStatus.fromString("PENDING_DELETE"))
    }

    /**
     * Проверяет защитный fallback для неизвестных строковых статусов.
     */
    @Test
    fun fromStringFallsBackToSyncedForUnknownValue() {
        // Неизвестное значение не должно ронять приложение при чтении старых или поврежденных данных.
        assertEquals(SyncStatus.SYNCED, SyncStatus.fromString("UNKNOWN"))
    }
}
