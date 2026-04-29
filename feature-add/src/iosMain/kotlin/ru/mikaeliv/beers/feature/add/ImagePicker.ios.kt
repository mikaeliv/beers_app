package ru.mikaeliv.beers.feature.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker {
    return remember {
        object : ImagePicker {
            override fun launch() = Unit
        }
    }
}
