package ru.mikaeliv.beers.composeDS.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.abv_badge
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mikaeliv.beers.composeDS.theme.BeersTheme

@Composable
fun AbvBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(999.dp))
            .padding(horizontal = 18.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onSecondary,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Preview
@Composable
private fun AbvBadgePreview() {
    BeersTheme {
        AbvBadge(text = stringResource(Res.string.abv_badge, "6.5"))
    }
}
