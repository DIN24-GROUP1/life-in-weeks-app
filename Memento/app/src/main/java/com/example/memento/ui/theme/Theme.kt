package com.example.memento.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary              = BrandViolet,
    onPrimary            = BrandLightSurface,
    primaryContainer     = BrandVioletDeep,
    onPrimaryContainer   = BrandLavender,
    secondary            = BrandVioletLight,
    onSecondary          = BrandDarkNavy,
    secondaryContainer   = BrandVioletDeep,
    onSecondaryContainer = BrandLavender,
    background           = BrandDarkNavy,
    onBackground         = BrandTextDark,
    surface              = BrandNavySurface,
    onSurface            = BrandTextDark,
    surfaceVariant       = BrandNavySurface2,
    onSurfaceVariant     = BrandMutedDark,
    surfaceContainer     = BrandNavySurface,
    outline              = BrandNavyBorder,
)

private val LightColorScheme = lightColorScheme(
    primary              = BrandViolet,
    onPrimary            = BrandLightSurface,
    primaryContainer     = BrandLavender,
    onPrimaryContainer   = BrandVioletDeep,
    secondary            = BrandVioletDark,
    onSecondary          = BrandLightSurface,
    secondaryContainer   = BrandLavender,
    onSecondaryContainer = BrandVioletDeep,
    background           = BrandLightBg,
    onBackground         = BrandTextLight,
    surface              = BrandLightSurface,
    onSurface            = BrandTextLight,
    surfaceVariant       = BrandLightSurface2,
    onSurfaceVariant     = BrandMutedLight,
    surfaceContainer     = BrandLightContainer,
    outline              = BrandLightBorder,
)

@Composable
fun MementoTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.Dark   -> true
        ThemeMode.Light  -> false
        ThemeMode.System -> isSystemInDarkTheme()
    }

    val appColors = if (darkTheme) darkAppColors() else lightAppColors()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
