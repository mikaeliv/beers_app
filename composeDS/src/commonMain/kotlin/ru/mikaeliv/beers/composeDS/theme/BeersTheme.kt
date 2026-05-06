package ru.mikaeliv.beers.composeDS.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val ANIMATION_DURATION = 500

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE8915B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF9E8DD),
    onPrimaryContainer = Color(0xFF3D3530),

    secondary = Color(0xFFF5EDE4),
    onSecondary = Color(0xFF6B5D54),
    secondaryContainer = Color(0xFFF5EDE4),
    onSecondaryContainer = Color(0xFF6B5D54),

    tertiary = Color(0xFFF4A261),
    onTertiary = Color(0xFF3D3530),
    tertiaryContainer = Color(0xFFF8EFE8),
    onTertiaryContainer = Color(0xFF6B5D54),

    error = Color(0xFFE76F51),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFE76F51),
    onErrorContainer = Color(0xFFFFFFFF),

    background = Color(0xFFFAF8F5),
    onBackground = Color(0xFF3D3530),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF3D3530),

    surfaceVariant = Color(0xFFF5EDE4),
    onSurfaceVariant = Color(0xFF9A8C82),

    outline = Color(0xFFF0E8DD),
    outlineVariant = Color(0xFFF7F1EA),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF4A261),
    onPrimary = Color(0xFF1F1B18),
    primaryContainer = Color(0xFF3A2A20),
    onPrimaryContainer = Color(0xFFF5EDE4),

    secondary = Color(0xFF3D352F),
    onSecondary = Color(0xFFE5DDD2),
    secondaryContainer = Color(0xFF3D352F),
    onSecondaryContainer = Color(0xFFE5DDD2),

    tertiary = Color(0xFFE8915B),
    onTertiary = Color(0xFFF5EDE4),
    tertiaryContainer = Color(0xFF2A2320),
    onTertiaryContainer = Color(0xFFE5DDD2),

    error = Color(0xFFE76F51),
    onError = Color(0xFFF5EDE4),
    errorContainer = Color(0xFFE76F51),
    onErrorContainer = Color(0xFFF5EDE4),

    background = Color(0xFF1F1B18),
    onBackground = Color(0xFFF5EDE4),

    surface = Color(0xFF2A2320),
    onSurface = Color(0xFFF5EDE4),

    surfaceVariant = Color(0xFF3D352F),
    onSurfaceVariant = Color(0xFF9A8C82),

    outline = Color(0xFF3D352F),
    outlineVariant = Color(0xFF2A2320),
)

private val BeersTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
        titleSmall = titleSmall.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
        bodyLarge = bodyLarge.copy(fontSize = 20.sp, lineHeight = 30.sp, fontWeight = FontWeight.Normal),
        bodyMedium = bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
        bodySmall = bodySmall.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        labelLarge = labelLarge.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
        labelMedium = labelMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    )
}

private val BeersShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
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
            typography = BeersTypography,
            shapes = BeersShapes,
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
