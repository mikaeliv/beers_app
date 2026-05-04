package ru.mikaeliv.beers.composeDS.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private const val ANIMATION_DURATION = 500

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),

    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF78350F),
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),

    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF78350F),
    tertiaryContainer = Color(0xFFF59E0B),
    onTertiaryContainer = Color(0xFF78350F),

    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFDC2626),
    onErrorContainer = Color(0xFFFFFFFF),

    background = Color(0xFFFDFBF7),
    onBackground = Color(0xFF2D1F16),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2D1F16),

    surfaceVariant = Color(0xFFF5F1E8),
    onSurfaceVariant = Color(0xFF78716C),

    outline = Color(0xFFE7E5DF),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color(0xFF1C1410),
    primaryContainer = Color(0xFF44281D),
    onPrimaryContainer = Color(0xFFFEF3C7),

    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF1C1410),
    secondaryContainer = Color(0xFF44281D),
    onSecondaryContainer = Color(0xFFFEF3C7),

    tertiary = Color(0xFF78350F),
    onTertiary = Color(0xFFFEF3C7),
    tertiaryContainer = Color(0xFF92400E),
    onTertiaryContainer = Color(0xFFFEF3C7),

    error = Color(0xFFEF4444),
    onError = Color(0xFFFEF3C7),
    errorContainer = Color(0xFFEF4444),
    onErrorContainer = Color(0xFFFEF3C7),

    background = Color(0xFF1C1410),
    onBackground = Color(0xFFFEF3C7),

    surface = Color(0xFF2D1F16),
    onSurface = Color(0xFFFEF3C7),

    surfaceVariant = Color(0xFF44281D),
    onSurfaceVariant = Color(0xFFA8A29E),

    outline = Color(0xFF44281D),
)

/**
 * Тема приложения Beers с анимацией смены цветов.
 *
 * @param darkTheme использовать тёмную тему
 * @param content контент приложения
 */
@Composable
fun BeersTheme(
    darkTheme: Boolean = ThemeState.isDarkTheme,
    content: @Composable () -> Unit
) {
    val targetColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val animatedColorScheme = animateColorScheme(targetColorScheme)

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
            content = content
        )
    }
}

@Composable
private fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
    val animationSpec = tween<Color>(durationMillis = ANIMATION_DURATION)
    
    return ColorScheme(
        primary = animateColorAsState(targetColorScheme.primary, animationSpec).value,
        onPrimary = animateColorAsState(targetColorScheme.onPrimary, animationSpec).value,
        primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, animationSpec).value,
        onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec).value,
        inversePrimary = animateColorAsState(targetColorScheme.inversePrimary, animationSpec).value,
        secondary = animateColorAsState(targetColorScheme.secondary, animationSpec).value,
        onSecondary = animateColorAsState(targetColorScheme.onSecondary, animationSpec).value,
        secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, animationSpec).value,
        onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec).value,
        tertiary = animateColorAsState(targetColorScheme.tertiary, animationSpec).value,
        onTertiary = animateColorAsState(targetColorScheme.onTertiary, animationSpec).value,
        tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec).value,
        onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec).value,
        background = animateColorAsState(targetColorScheme.background, animationSpec).value,
        onBackground = animateColorAsState(targetColorScheme.onBackground, animationSpec).value,
        surface = animateColorAsState(targetColorScheme.surface, animationSpec).value,
        onSurface = animateColorAsState(targetColorScheme.onSurface, animationSpec).value,
        surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, animationSpec).value,
        onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec).value,
        surfaceTint = animateColorAsState(targetColorScheme.surfaceTint, animationSpec).value,
        inverseSurface = animateColorAsState(targetColorScheme.inverseSurface, animationSpec).value,
        inverseOnSurface = animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec).value,
        error = animateColorAsState(targetColorScheme.error, animationSpec).value,
        onError = animateColorAsState(targetColorScheme.onError, animationSpec).value,
        errorContainer = animateColorAsState(targetColorScheme.errorContainer, animationSpec).value,
        onErrorContainer = animateColorAsState(targetColorScheme.onErrorContainer, animationSpec).value,
        outline = animateColorAsState(targetColorScheme.outline, animationSpec).value,
        outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, animationSpec).value,
        scrim = animateColorAsState(targetColorScheme.scrim, animationSpec).value,
        surfaceBright = animateColorAsState(targetColorScheme.surfaceBright, animationSpec).value,
        surfaceDim = animateColorAsState(targetColorScheme.surfaceDim, animationSpec).value,
        surfaceContainer = animateColorAsState(targetColorScheme.surfaceContainer, animationSpec).value,
        surfaceContainerHigh = animateColorAsState(targetColorScheme.surfaceContainerHigh, animationSpec).value,
        surfaceContainerHighest = animateColorAsState(targetColorScheme.surfaceContainerHighest, animationSpec).value,
        surfaceContainerLow = animateColorAsState(targetColorScheme.surfaceContainerLow, animationSpec).value,
        surfaceContainerLowest = animateColorAsState(targetColorScheme.surfaceContainerLowest, animationSpec).value,
    )
}
