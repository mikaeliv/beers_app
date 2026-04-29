package ru.mikaeliv.beers.feature.add

import androidx.compose.runtime.Composable

interface ImagePicker {
    fun launch()
}

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker
