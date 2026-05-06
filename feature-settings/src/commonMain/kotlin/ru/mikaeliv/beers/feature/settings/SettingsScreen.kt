package ru.mikaeliv.beers.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.settings_title
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.components.BeersTopAppBar
import ru.mikaeliv.beers.composeDS.components.RoundIconSurface

@Composable
fun SettingsScreen(component: SettingsComponent) {
    val state by component.state.subscribeAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BeersTopAppBar(
                title = stringResource(Res.string.settings_title),
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
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(162.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeOption(
                            title = "Light",
                            selected = !state.isDarkTheme,
                            modifier = Modifier.weight(1f),
                            onClick = { component.onDarkThemeToggle(false) }
                        ) {
                            SunIcon(tint = MaterialTheme.colorScheme.primary)
                        }
                        ThemeOption(
                            title = "Dark",
                            selected = state.isDarkTheme,
                            modifier = Modifier.weight(1f),
                            onClick = { component.onDarkThemeToggle(true) }
                        ) {
                            MoonIcon(tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(116.dp))
                Text(
                    text = "BeerLog v1.0.0\nYour craft beer journal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    )
}

@Composable
private fun ThemeOption(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                RoundedCornerShape(26.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 18.dp)
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("v", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium)
            }
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            RoundIconSurface(size = 58.dp, selected = selected) { icon() }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SunIcon(
    modifier: Modifier = Modifier.size(30.dp),
    tint: Color,
) {
    Canvas(modifier = modifier) {
        drawCircle(tint, radius = size.minDimension * 0.18f, style = Stroke(size.minDimension * 0.08f))
        val center = Offset(size.width / 2f, size.height / 2f)
        val inner = size.minDimension * 0.32f
        val outer = size.minDimension * 0.46f
        repeat(8) { index ->
            val angle = (index * 45.0 * kotlin.math.PI / 180.0)
            drawLine(
                color = tint,
                start = Offset(center.x + kotlin.math.cos(angle).toFloat() * inner, center.y + kotlin.math.sin(angle).toFloat() * inner),
                end = Offset(center.x + kotlin.math.cos(angle).toFloat() * outer, center.y + kotlin.math.sin(angle).toFloat() * outer),
                strokeWidth = size.minDimension * 0.07f
            )
        }
    }
}

@Composable
private fun MoonIcon(
    modifier: Modifier = Modifier.size(30.dp),
    tint: Color,
) {
    val cutout = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        drawCircle(tint, center = Offset(size.width * 0.45f, size.height * 0.5f), radius = size.minDimension * 0.32f, style = Stroke(size.minDimension * 0.08f))
        drawCircle(cutout, center = Offset(size.width * 0.58f, size.height * 0.38f), radius = size.minDimension * 0.32f)
    }
}
