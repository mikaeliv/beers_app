package ru.mikaeliv.beers.feature.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.beer_list_empty
import beers.composeds.generated.resources.beer_list_empty_hint
import beers.composeds.generated.resources.beer_list_title
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.icons.AddIcon
import ru.mikaeliv.beers.composeDS.icons.PersonIcon
import ru.mikaeliv.beers.core.Beer

private const val LOAD_MORE_THRESHOLD = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerListScreen(component: BeerListComponent) {
    val beers by component.state.subscribeAsState()
    val isRefreshing by component.isRefreshing.collectAsState()
    val hasMorePages by component.hasMorePages.collectAsState()
    val isLoadingMore by component.isLoadingMore.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.beer_list_title)) },
                actions = {
                    IconButton(onClick = component::onProfileClick) {
                        PersonIcon()
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = component::onAddClick) {
                AddIcon(tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullToRefresh(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = component::onRefresh
                )
        ) {
            BeerListContent(
                beers = beers,
                isRefreshing = isRefreshing,
                listState = listState,
                hasMorePages = hasMorePages,
                isLoadingMore = isLoadingMore,
                onOpen = component::onOpen,
                onLoadMore = component::onLoadMore
            )
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = pullToRefreshState
            )
        }
    }
}

@Composable
private fun BeerListContent(
    beers: List<Beer>,
    isRefreshing: Boolean,
    listState: LazyListState,
    hasMorePages: Boolean,
    isLoadingMore: Boolean,
    onOpen: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    // Загрузка следующей страницы при прокрутке к концу
    LaunchedEffect(listState, hasMorePages, isLoadingMore) {
        val layoutInfo = listState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        if (
            totalItems > 0 &&
            lastVisibleItem >= totalItems - LOAD_MORE_THRESHOLD &&
            hasMorePages &&
            !isLoadingMore
        ) {
            onLoadMore()
        }
    }

    if (beers.isEmpty()) {
        if (isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(Res.string.beer_list_empty), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(Res.string.beer_list_empty_hint), style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(beers) { beer ->
            BeerRow(beer = beer, onClick = { beer.id?.let(onOpen) })
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun BeerRow(beer: Beer, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(beer.name, style = MaterialTheme.typography.titleMedium)
        Text(
            "ABV ${beer.abv} • ★ ${beer.rating}",
            style = MaterialTheme.typography.bodyMedium
        )
        if (!beer.comment.isNullOrBlank()) {
            Text(beer.comment!!, style = MaterialTheme.typography.bodySmall)
        }
    }
}
