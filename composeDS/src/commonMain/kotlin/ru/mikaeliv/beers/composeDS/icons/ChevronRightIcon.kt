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
import beers.composeds.generated.resources.ic_chevron_right
import beers.composeds.generated.resources.icon_chevron_right
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Иконка "Вперед".
 *
 * @param modifier модификатор
 * @param size размер иконки
 * @param tint цвет иконки (по умолчанию onSurfaceVariant)
 */
@Composable
fun ChevronRightIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Image(
        painter = painterResource(Res.drawable.ic_chevron_right),
        contentDescription = stringResource(Res.string.icon_chevron_right),
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size)
    )
}
