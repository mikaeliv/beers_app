package ru.mikaeliv.beers.composeDS.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mikaeliv.beers.composeDS.theme.BeersTheme

@Composable
fun BeerMugIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.075f
        val mugTop = size.height * 0.34f
        val mugLeft = size.width * 0.22f
        val mugSize = Size(size.width * 0.48f, size.height * 0.52f)

        drawRoundRect(
            color = tint,
            topLeft = Offset(mugLeft, mugTop),
            size = mugSize,
            cornerRadius = CornerRadius(size.width * 0.08f),
            style = Stroke(stroke)
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.69f, size.height * 0.47f),
            size = Size(size.width * 0.20f, size.height * 0.22f),
            cornerRadius = CornerRadius(size.width * 0.10f),
            style = Stroke(stroke)
        )

        val foam = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.31f)
            cubicTo(size.width * 0.18f, size.height * 0.18f, size.width * 0.34f, size.height * 0.15f, size.width * 0.39f, size.height * 0.23f)
            cubicTo(size.width * 0.45f, size.height * 0.13f, size.width * 0.58f, size.height * 0.13f, size.width * 0.63f, size.height * 0.23f)
            cubicTo(size.width * 0.76f, size.height * 0.15f, size.width * 0.88f, size.height * 0.31f, size.width * 0.73f, size.height * 0.38f)
            cubicTo(size.width * 0.60f, size.height * 0.43f, size.width * 0.37f, size.height * 0.41f, size.width * 0.22f, size.height * 0.31f)
        }
        drawPath(foam, tint, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        drawLine(tint, Offset(size.width * 0.38f, size.height * 0.48f), Offset(size.width * 0.38f, size.height * 0.73f), stroke * 0.72f)
        drawLine(tint, Offset(size.width * 0.55f, size.height * 0.48f), Offset(size.width * 0.55f, size.height * 0.73f), stroke * 0.72f)
    }
}

@Preview
@Composable
private fun BeerMugIconPreview() {
    BeersTheme {
        BeerMugIcon(modifier = Modifier.size(64.dp))
    }
}
