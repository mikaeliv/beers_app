package ru.mikaeliv.beers.network.dto

import kotlinx.serialization.Serializable

/**
 * Запрос на регистрацию/авторизацию.
 */
@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
)

/**
 * Запрос на обновление токенов.
 */
@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

/**
 * Ответ с токенами авторизации.
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
)
