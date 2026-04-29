package ru.mikaeliv.beers.composeDS.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun BeerPhoto(
    photoBytes: ByteArray?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val imageBitmap = remember(photoBytes) {
        photoBytes?.let {
            runCatching { it.decodeToImageBitmap() }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            DefaultBeerPhoto(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun DefaultBeerPhoto(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val glass = Color(0xFFB7791F)
            val foam = Color(0xFFFFF7D6)
            val outline = Color(0xFF5F4325)
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = glass,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.3f),
                size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f)
            )
            drawRoundRect(
                color = foam,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.22f),
                size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f)
            )
            drawRoundRect(
                color = outline.copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.42f),
                size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.035f)
            )
            drawRoundRect(
                color = outline.copy(alpha = 0.35f),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.3f),
                size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.025f)
            )
        }
    }
}
