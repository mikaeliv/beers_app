package ru.mikaeliv.beers.feature.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.abv_badge
import beers.composeds.generated.resources.beer_list_empty
import beers.composeds.generated.resources.beer_list_empty_hint
import beers.composeds.generated.resources.beer_list_title
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.components.AbvBadge
import ru.mikaeliv.beers.composeDS.components.BeerPhoto
import ru.mikaeliv.beers.composeDS.components.RoundIconSurface
import ru.mikaeliv.beers.composeDS.components.StarRating
import ru.mikaeliv.beers.composeDS.icons.AddIcon
import ru.mikaeliv.beers.composeDS.icons.PersonIcon
import ru.mikaeliv.beers.composeDS.icons.RefreshIcon
import ru.mikaeliv.beers.core.Beer

private const val LOAD_MORE_THRESHOLD = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerListScreen(component: BeerListComponent) {
    val beers by component.state.subscribeAsState()
    val isRefreshing by component.isRefreshing.collectAsState()
    val isLoadingMore by component.isLoadingMore.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BeerListTopBar(
                onProfileClick = component::onProfileClick,
                onRefresh = component::onRefresh,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = component::onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                AddIcon(tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .pullToRefresh(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = component::onRefresh
                )
        ) {
            BeerGridContent(
                beers = beers,
                isRefreshing = isRefreshing,
                listState = listState,
                isLoadingMore = isLoadingMore,
                onOpen = component::onOpen,
                onLoadMore = component::onLoadMore,
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
private fun BeerGridContent(
    beers: List<Beer>,
    isRefreshing: Boolean,
    listState: LazyListState,
    isLoadingMore: Boolean,
    onOpen: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            LoadMoreViewport(
                totalItems = layoutInfo.totalItemsCount,
                lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1,
            )
        }
            .distinctUntilChanged()
            .collect { viewport ->
                val reachedThreshold = viewport.totalItems > 0 &&
                    viewport.lastVisibleItem >= viewport.totalItems - LOAD_MORE_THRESHOLD

                if (reachedThreshold) {
                    onLoadMore()
                }
            }
        }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        if (beers.isEmpty()) {
            item {
                EmptyState(isRefreshing = isRefreshing)
            }
        } else {
            items(beers.chunked(2)) { rowBeers ->
                BeerCardRow(
                    beers = rowBeers,
                    onOpen = onOpen
                )
            }
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun BeerCardRow(
    beers: List<Beer>,
    onOpen: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        beers.forEach { beer ->
            BeerCard(
                beer = beer,
                onClick = { beer.id?.let(onOpen) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        if (beers.size == 1) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun BeerListTopBar(
    onProfileClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .height(100.dp)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(Res.string.beer_list_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (showManualRefreshButton) {
                RoundIconSurface(onClick = onRefresh) {
                    RefreshIcon(tint = MaterialTheme.colorScheme.primary)
                }
            }
            RoundIconSurface(onClick = onProfileClick) {
                PersonIcon(tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun EmptyState(isRefreshing: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 96.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.beer_list_empty), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(Res.string.beer_list_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BeerCard(
    beer: Beer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 7.dp
    ) {
        Column {
            BeerPhoto(
                photoBytes = beer.photoBytes,
                contentDescription = beer.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = beer.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                AbvBadge(stringResource(Res.string.abv_badge, beer.abv.toString()))
                StarRating(rating = beer.rating, starSize = 20.dp)
            }
        }
    }
}
