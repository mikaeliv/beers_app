package ru.mikaeliv.beers.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.profile_logout
import beers.composeds.generated.resources.profile_settings
import beers.composeds.generated.resources.profile_title
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.components.BeersButton
import ru.mikaeliv.beers.composeDS.components.BeersTopAppBar
import ru.mikaeliv.beers.composeDS.components.RoundIconSurface
import ru.mikaeliv.beers.composeDS.components.StarRating
import ru.mikaeliv.beers.composeDS.icons.BeerIcon
import ru.mikaeliv.beers.composeDS.icons.LogoutIcon
import ru.mikaeliv.beers.composeDS.icons.PersonIcon
import ru.mikaeliv.beers.composeDS.icons.SettingsIcon

@Composable
fun ProfileScreen(component: ProfileComponent) {
    val state by component.state.subscribeAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BeersTopAppBar(
                title = stringResource(Res.string.profile_title),
                onBack = component::onBack
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                RoundIconSurface(size = 132.dp, selected = true) {
                    PersonIcon(size = 62.dp, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "Beer Enthusiast",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.email,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(58.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    StatCard("24", "Beers", modifier = Modifier.weight(1f)) {
                        BeerIcon(size = 24.dp, tint = MaterialTheme.colorScheme.primary)
                    }
                    StatCard("4.2", "Rating", modifier = Modifier.weight(1f)) {
                        StarRating(rating = 1, maxRating = 1, starSize = 24.dp)
                    }
                    StatCard("12", "Favorites", modifier = Modifier.weight(1f)) {
                        BeerIcon(size = 24.dp, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(58.dp))
                SettingsRow(onClick = component::onSettingsClick)
                Spacer(modifier = Modifier.height(46.dp))
                BeersButton(
                    text = stringResource(Res.string.profile_logout),
                    onClick = component::onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    leadingIcon = {
                        LogoutIcon(size = 24.dp, tint = MaterialTheme.colorScheme.onError)
                    }
                )
            }
        }
    )
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.height(142.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RoundIconSurface(size = 48.dp, selected = true) { icon() }
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 16.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsRow(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconSurface(size = 56.dp) {
                SettingsIcon(size = 26.dp, tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = stringResource(Res.string.profile_settings),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 22.dp)
            )
            Text(">", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
