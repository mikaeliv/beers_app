package ru.mikaeliv.beers.composeDS.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.ic_visibility_off
import beers.composeds.generated.resources.icon_visibility_off
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Иконка "Скрыть пароль" (глаз перечёркнут).
 */
@Composable
fun VisibilityOffIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Image(
        painter = painterResource(Res.drawable.ic_visibility_off),
        contentDescription = stringResource(Res.string.icon_visibility_off),
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size)
    )
}
