package ru.mikaeliv.beers.composeDS.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mikaeliv.beers.composeDS.theme.BeersTheme

@Composable
fun RoundIconSurface(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                CircleShape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
private fun RoundIconSurfacePreview() {
    BeersTheme {
        RoundIconSurface {
            BeerMugIcon(modifier = Modifier.size(24.dp))
        }
    }
}
