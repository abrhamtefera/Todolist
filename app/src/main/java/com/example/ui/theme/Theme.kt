package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BentoPrimaryDark,
    onPrimary = BentoOnPrimaryDark,
    primaryContainer = BentoPrimaryContainerDark,
    onPrimaryContainer = BentoOnPrimaryContainerDark,
    secondary = BentoSecondaryDark,
    secondaryContainer = BentoSecondaryContainerDark,
    onSecondaryContainer = BentoOnSecondaryContainerDark,
    background = BentoBackgroundDark,
    surface = BentoSurfaceDark,
    surfaceVariant = BentoSurfaceVariantDark,
    onSurface = BentoOnSurfaceDark,
    onSurfaceVariant = BentoOnSurfaceVariantDark,
    outline = BentoOutlineDark,
    error = BentoErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimaryLight,
    onPrimary = BentoOnPrimaryLight,
    primaryContainer = BentoPrimaryContainerLight,
    onPrimaryContainer = BentoOnPrimaryContainerLight,
    secondary = BentoSecondaryLight,
    secondaryContainer = BentoSecondaryContainerLight,
    onSecondaryContainer = BentoOnSecondaryContainerLight,
    background = BentoBackgroundLight,
    surface = BentoSurfaceLight,
    surfaceVariant = BentoSurfaceVariantLight,
    onSurface = BentoOnSurfaceLight,
    onSurfaceVariant = BentoOnSurfaceVariantLight,
    outline = BentoOutlineLight,
    error = BentoErrorLight
)

@Composable
fun MyApplicationTheme(
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
