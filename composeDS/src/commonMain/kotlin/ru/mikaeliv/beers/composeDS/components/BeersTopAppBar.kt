package ru.mikaeliv.beers.composeDS.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.ic_arrow_back
import beers.composeds.generated.resources.icon_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mikaeliv.beers.composeDS.theme.BeersTheme

/**
 * Переиспользуемый TopAppBar для приложения.
 * Кастомная реализация для корректной анимации смены темы.
 *
 * @param title заголовок тулбара
 * @param onBack колбэк при нажатии на кнопку "назад" (если null — кнопка не отображается)
 * @param modifier модификатор
 */
@Composable
fun BeersTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .height(96.dp)
            .padding(horizontal = 24.dp)
    ) {
        if (onBack != null) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.icon_back),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview
@Composable
private fun BeersTopAppBarPreview() {
    BeersTheme {
        BeersTopAppBar(
            title = "Add Beer",
            onBack = {}
        )
    }
}
