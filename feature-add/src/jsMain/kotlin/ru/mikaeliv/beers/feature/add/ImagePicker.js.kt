package ru.mikaeliv.beers.feature.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker {
    val currentOnImagePicked = rememberUpdatedState(onImagePicked)
    return remember {
        object : ImagePicker {
            override fun launch() {
                val input = document.createElement("input") as HTMLInputElement
                input.type = "file"
                input.accept = "image/*"
                input.onchange = {
                    val file = input.files?.item(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = {
                            val array = Uint8Array(reader.result as ArrayBuffer)
                            val bytes = ByteArray(array.length)
                            for (index in 0 until array.length) {
                                bytes[index] = (array.asDynamic()[index] as Int).toByte()
                            }
                            currentOnImagePicked.value(bytes)
                            null
                        }
                        reader.readAsArrayBuffer(file)
                    }
                    null
                }
                input.click()
            }
        }
    }
}
