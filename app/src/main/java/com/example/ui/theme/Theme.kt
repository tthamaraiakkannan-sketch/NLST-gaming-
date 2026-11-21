package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val NlstDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = ObsidianBlack,
    primaryContainer = NeonGreenSoft,
    onPrimaryContainer = NeonGreen,
    secondary = ElectricPurple,
    onSecondary = TextPrimary,
    secondaryContainer = ElectricPurpleSoft,
    onSecondaryContainer = ElectricPurple,
    tertiary = CyberCyan,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = ObsidianElevated,
    outline = ObsidianBorder,
    error = LiveCrimson,
    onError = TextPrimary,
    errorContainer = LiveCrimsonSoft,
    onErrorContainer = LiveCrimson,
)

private val NlstShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun NlstGamingTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = NlstDarkColorScheme,
        typography = NlstTypography,
        shapes = NlstShapes,
        content = content,
    )
}
