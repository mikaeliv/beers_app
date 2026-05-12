package ru.mikaeliv.beers.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.camera_android_only
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun CameraPreview(
    captureRequest: Int,
    onPhotoCaptured: (ByteArray) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.camera_android_only),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
