package ru.mikaeliv.beers.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.beer_detail_not_found
import beers.composeds.generated.resources.beer_detail_rating_label
import beers.composeds.generated.resources.beer_detail_title
import beers.composeds.generated.resources.delete
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.components.BeerPhoto
import ru.mikaeliv.beers.composeDS.components.BeersTopAppBar
import ru.mikaeliv.beers.composeDS.components.StarRating

@Composable
fun BeerDetailScreen(component: BeerDetailComponent) {
    val state by component.state.subscribeAsState()

    Scaffold(
        topBar = {
            BeersTopAppBar(
                title = stringResource(Res.string.beer_detail_title),
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
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
            return
        }

        val beer = state.beer
        if (beer == null) {
            Text(stringResource(Res.string.beer_detail_not_found), style = MaterialTheme.typography.titleMedium)
            return
        }

        Text(beer.name, style = MaterialTheme.typography.headlineSmall)
        BeerPhoto(
            photoBytes = beer.photoBytes,
            contentDescription = beer.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
        Text("ABV ${beer.abv}%", style = MaterialTheme.typography.bodyLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(Res.string.beer_detail_rating_label), style = MaterialTheme.typography.bodyLarge)
            StarRating(rating = beer.rating)
        }
        if (!beer.comment.isNullOrBlank()) {
            Text(beer.comment!!, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDelete
        ) { Text(stringResource(Res.string.delete)) }
    }
}
