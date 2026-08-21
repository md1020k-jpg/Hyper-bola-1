package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CleanMinDarkPrimary,
    onPrimary = CleanMinDarkOnPrimary,
    primaryContainer = CleanMinDarkPrimaryContainer,
    onPrimaryContainer = CleanMinDarkOnPrimaryContainer,
    secondary = CleanMinDarkSecondary,
    onSecondary = CleanMinDarkOnPrimary,
    secondaryContainer = CleanMinDarkSecondaryContainer,
    onSecondaryContainer = CleanMinDarkOnSecondaryContainer,
    tertiary = CleanMinDarkTertiary,
    onTertiary = CleanMinDarkOnTertiaryContainer,
    tertiaryContainer = CleanMinDarkTertiaryContainer,
    onTertiaryContainer = CleanMinDarkOnTertiaryContainer,
    background = CleanMinDarkBackground,
    onBackground = CleanMinDarkOnBackground,
    surface = CleanMinDarkSurface,
    onSurface = CleanMinDarkOnSurface,
    surfaceVariant = CleanMinDarkSurfaceContainer,
    onSurfaceVariant = CleanMinDarkOnSurfaceVariant,
    surfaceContainer = CleanMinDarkSurfaceContainer,
    surfaceContainerHigh = CleanMinDarkSurfaceContainerHigh,
    surfaceContainerHighest = CleanMinDarkSurfaceContainerHighest,
    outline = CleanMinDarkOutline,
    outlineVariant = CleanMinDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = CleanMinPrimary,
    onPrimary = CleanMinOnPrimary,
    primaryContainer = CleanMinPrimaryContainer,
    onPrimaryContainer = CleanMinOnPrimaryContainer,
    secondary = CleanMinSecondary,
    onSecondary = CleanMinOnSecondary,
    secondaryContainer = CleanMinSecondaryContainer,
    onSecondaryContainer = CleanMinOnSecondaryContainer,
    tertiary = CleanMinTertiary,
    onTertiary = CleanMinOnTertiary,
    tertiaryContainer = CleanMinTertiaryContainer,
    onTertiaryContainer = CleanMinOnTertiaryContainer,
    background = CleanMinBackground,
    onBackground = CleanMinOnBackground,
    surface = CleanMinSurface,
    onSurface = CleanMinOnSurface,
    surfaceVariant = CleanMinSurfaceContainer,
    onSurfaceVariant = CleanMinOnSurfaceVariant,
    surfaceContainer = CleanMinSurfaceContainer,
    surfaceContainerHigh = CleanMinSurfaceContainerHigh,
    surfaceContainerHighest = CleanMinSurfaceContainerHighest,
    surfaceContainerLow = CleanMinSurfaceContainerLow,
    outline = CleanMinOutline,
    outlineVariant = CleanMinOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
