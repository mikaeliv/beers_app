package ru.mikaeliv.beers.feature.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker {
    val currentOnImagePicked = rememberUpdatedState(onImagePicked)
    return remember {
        object : ImagePicker {
            override fun launch() {
                val dialog = FileDialog(null as Frame?, "Choose beer photo", FileDialog.LOAD)
                dialog.file = "*.jpg;*.jpeg;*.png;*.webp"
                dialog.isVisible = true

                val directory = dialog.directory ?: return
                val fileName = dialog.file ?: return
                val file = java.io.File(directory, fileName)
                if (file.isFile) {
                    currentOnImagePicked.value(file.readBytes())
                }
            }
        }
    }
}
