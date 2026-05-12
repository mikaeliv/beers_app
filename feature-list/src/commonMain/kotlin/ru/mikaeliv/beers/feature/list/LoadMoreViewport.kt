package ru.mikaeliv.beers.feature.list

internal data class LoadMoreViewport(
    val totalItems: Int,
    val lastVisibleItem: Int,
)
