package com.tim.autotask.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = BlueDim,
    onPrimaryContainer = Blue,
    secondary = Purple,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = PurpleDim,
    onSecondaryContainer = Purple,
    tertiary = Amber,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = AmberDim,
    onTertiaryContainer = Amber,
    error = Red,
    onError = Color(0xFFFFFFFF),
    errorContainer = RedDim,
    onErrorContainer = Red,
    background = LightBg,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightOnSurface2,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceContainerLow = LightSurface2,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = BlueDim80,
    onPrimaryContainer = Blue80,
    secondary = Purple80,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = PurpleDim80,
    onSecondaryContainer = Purple80,
    tertiary = Amber80,
    onTertiary = Color(0xFF1A1D2E),
    tertiaryContainer = AmberDim80,
    onTertiaryContainer = Amber80,
    error = Red80,
    onError = Color(0xFF1A1D2E),
    errorContainer = RedDim80,
    onErrorContainer = Red80,
    background = DarkBg,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkOnSurface2,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceContainerLow = DarkSurface2,
)

@Composable
fun AutoTaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
