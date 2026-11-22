package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BaonLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = SageContainer,
    onPrimaryContainer = OnSageContainer,
    secondary = SoftSageSecondary,
    onSecondary = Color.White,
    secondaryContainer = PastelSageContainer,
    onSecondaryContainer = OnPastelSageContainer,
    tertiary = WarmGoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = WarmGoldContainer,
    onTertiaryContainer = OnWarmGoldContainer,
    background = CreamBackground,
    onBackground = ForestInk,
    surface = CreamSurface,
    onSurface = ForestInk,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = SageMutedInk,
    surfaceContainerLowest = PureWarmWhite,
    surfaceContainerLow = CreamBackground,
    surfaceContainer = CreamSurface,
    surfaceContainerHigh = PastelSageContainer,
    surfaceContainerHighest = CreamSurfaceVariant,
    outline = SageOutline,
    outlineVariant = SageOutlineVariant,
    error = DangerRose,
    onError = Color.White,
    errorContainer = DangerRoseContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val BaonDarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = DarkOnEmeraldPrimary,
    primaryContainer = DarkSageContainer,
    onPrimaryContainer = DarkOnSageContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnEmeraldPrimary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkInk,
    tertiary = Color(0xFFE7B868),
    onTertiary = Color(0xFF3E2700),
    tertiaryContainer = Color(0xFF56390A),
    onTertiaryContainer = WarmGoldContainer,
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedInk,
    surfaceContainerLowest = Color(0xFF0C120E),
    surfaceContainerLow = DarkBackground,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = Color(0xFF2B3D32),
    outline = DarkOutline,
    outlineVariant = Color(0xFF2A392F),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = DangerRoseContainer
)

val BaonShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light (Warm Cream)"),
    DARK("Dark (Forest Night)")
}

@Composable
fun BaonBuddyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) BaonDarkColorScheme else BaonLightColorScheme,
        typography = BaonTypography,
        shapes = BaonShapes,
        content = content
    )
}
