package ru.mikaeliv.beers.core

/**
 * Модель домена для пива, которую пользователь добавляет в приложение.
 *
 * @property id локальный идентификатор записи в базе (может быть null для новых объектов)
 * @property serverId идентификатор на сервере (null для несинхронизированных записей)
 * @property name название пива
 * @property abv крепость в % ABV
 * @property comment опциональный комментарий пользователя
 * @property rating оценка по пятибалльной шкале (1..5)
 * @property photoBytes фотография в виде массива байт (опционально)
 * @property syncStatus статус синхронизации с сервером
 */
data class Beer(
    val id: Long?,
    val serverId: String? = null,
    val name: String,
    val abv: Double,
    val comment: String?,
    val rating: Int, // 1..5
    val photoBytes: ByteArray? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)
