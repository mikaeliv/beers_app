package ru.mikaeliv.beers.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.beer_detail_not_found
import beers.composeds.generated.resources.delete
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.components.AbvBadge
import ru.mikaeliv.beers.composeDS.components.BeerPhoto
import ru.mikaeliv.beers.composeDS.components.BeersButton
import ru.mikaeliv.beers.composeDS.components.BeersTopAppBar
import ru.mikaeliv.beers.composeDS.components.StarRating

@Composable
fun BeerDetailScreen(component: BeerDetailComponent) {
    val state by component.state.subscribeAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BeersTopAppBar(
                title = "",
                onBack = component::onBack
            )
        }
    ) { padding ->
        BeerDetailContent(
            state = state,
            contentPadding = padding,
            onDelete = component::onDelete,
        )
    }
}

@Composable
private fun BeerDetailContent(
    state: BeerDetailState,
    contentPadding: PaddingValues,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        val beer = state.beer
        if (beer == null) {
            Text(stringResource(Res.string.beer_detail_not_found), style = MaterialTheme.typography.titleMedium)
            return
        }

        BeerPhoto(
            photoBytes = beer.photoBytes,
            contentDescription = beer.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.78f)
        )

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                beer.name,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            AbvBadge("${beer.abv}% ABV")
            StarRating(rating = beer.rating, starSize = 34.dp, modifier = Modifier.padding(top = 8.dp))
        }

        if (!beer.comment.isNullOrBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "TASTING NOTES",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    beer.comment!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f)
                )
            }
        }

        BeersButton(
            text = "${stringResource(Res.string.delete)} Beer",
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )
    }
}
