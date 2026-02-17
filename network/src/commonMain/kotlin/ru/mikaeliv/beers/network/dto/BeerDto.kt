package ru.mikaeliv.beers.network.dto

import kotlinx.serialization.Serializable

/**
 * Запрос на создание/обновление пива.
 */
@Serializable
data class BeerRequest(
    val id: String? = null,
    val name: String,
    val rating: Int,
    val abv: Double,
    val description: String? = null,
)

/**
 * Ответ с данными пива.
 */
@Serializable
data class BeerResponse(
    val id: String,
    val name: String,
    val rating: Int,
    val abv: Double,
    val description: String?,
    val createdAt: String, // ISO-8601 строка, парсится на стороне клиента при необходимости
)
