package ru.mikaeliv.beers.composeDS.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mikaeliv.beers.composeDS.icons.BeerIcon
import ru.mikaeliv.beers.composeDS.theme.BeersTheme

@Composable
fun BeerLogo(
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        BeerIcon(
            size = size * 0.44f,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
private fun BeerLogoPreview() {
    BeersTheme {
        BeerLogo()
    }
}
