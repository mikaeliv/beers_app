package ru.mikaeliv.beers.composeDS.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.ic_star
import beers.composeds.generated.resources.ic_star_outline
import beers.composeds.generated.resources.icon_star_empty
import beers.composeds.generated.resources.icon_star_filled
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mikaeliv.beers.composeDS.theme.BeersTheme

/**
 * Компонент отображения рейтинга в виде звёздочек.
 *
 * @param rating текущий рейтинг (1..5)
 * @param modifier модификатор для Row-контейнера
 * @param maxRating максимальное количество звёзд (по умолчанию 5)
 * @param starSize размер каждой звезды
 * @param onRatingChange колбэк при клике на звезду (если null — звёзды не кликабельны)
 */
@Composable
fun StarRating(
    rating: Int,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 24.dp,
    onRatingChange: ((Int) -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(maxRating) { index ->
            val starRating = index + 1
            val isFilled = index < rating
            val interactionSource = remember { MutableInteractionSource() }
            
            Image(
                painter = painterResource(
                    if (isFilled) Res.drawable.ic_star else Res.drawable.ic_star_outline
                ),
                contentDescription = stringResource(
                    if (isFilled) Res.string.icon_star_filled else Res.string.icon_star_empty
                ),
                colorFilter = ColorFilter.tint(
                    if (isFilled) {
                        androidx.compose.material3.MaterialTheme.colorScheme.primary
                    } else {
                        androidx.compose.material3.MaterialTheme.colorScheme.outline
                    }
                ),
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRatingChange != null) {
                            Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onRatingChange(starRating) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Preview
@Composable
private fun StarRatingPreview() {
    BeersTheme {
        StarRating(rating = 4, starSize = 32.dp)
    }
}
