package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PolishDarkPrimary,
    onPrimary = PolishDarkOnPrimary,
    primaryContainer = PolishDarkPrimaryContainer,
    onPrimaryContainer = PolishDarkOnPrimaryContainer,
    secondary = PolishDarkSecondary,
    onSecondary = PolishDarkOnSecondary,
    secondaryContainer = PolishDarkSecondaryContainer,
    onSecondaryContainer = PolishDarkOnSecondaryContainer,
    tertiary = PolishDarkTertiary,
    onTertiary = PolishDarkOnTertiary,
    background = PolishDarkBackground,
    onBackground = PolishDarkTextPrimary,
    surface = PolishDarkSurface,
    onSurface = PolishDarkTextPrimary,
    surfaceVariant = PolishDarkSurfaceVariant,
    onSurfaceVariant = PolishDarkTextSecondary,
    outline = PolishDarkCardBorder,
    outlineVariant = PolishDarkBorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = PolishLightPrimary,
    onPrimary = PolishLightOnPrimary,
    primaryContainer = PolishLightPrimaryContainer,
    onPrimaryContainer = PolishLightOnPrimaryContainer,
    secondary = PolishLightSecondary,
    onSecondary = PolishLightOnSecondary,
    secondaryContainer = PolishLightSecondaryContainer,
    onSecondaryContainer = PolishLightOnSecondaryContainer,
    tertiary = PolishLightTertiary,
    onTertiary = PolishLightOnTertiary,
    background = PolishLightBackground,
    onBackground = PolishLightTextPrimary,
    surface = PolishLightSurface,
    onSurface = PolishLightTextPrimary,
    surfaceVariant = PolishLightSurfaceVariant,
    onSurfaceVariant = PolishLightTextSecondary,
    outline = PolishLightCardBorder,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun SalmonAppTheme(
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
