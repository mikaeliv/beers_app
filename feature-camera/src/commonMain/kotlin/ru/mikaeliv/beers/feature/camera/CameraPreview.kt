package ru.mikaeliv.beers.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun CameraPreview(
    captureRequest: Int,
    onPhotoCaptured: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
)
