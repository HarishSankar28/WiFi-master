package com.wifisense.motiontracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary            = CyanPrimary,
    onPrimary          = NavyDeep,
    primaryContainer   = NavyElevated,
    onPrimaryContainer = CyanPrimary,

    secondary          = PurpleAccent,
    onSecondary        = TextPrimary,
    secondaryContainer = NavyCard,
    onSecondaryContainer = TextPrimary,

    tertiary           = MotionModerate,
    onTertiary         = NavyDeep,

    background         = NavyDeep,
    onBackground       = TextPrimary,

    surface            = NavySurface,
    onSurface          = TextPrimary,
    surfaceVariant     = NavyCard,
    onSurfaceVariant   = TextSecondary,

    outline            = NavyElevated,
    outlineVariant     = Color(0xFF1E2A4A),

    error              = Error,
    onError            = TextPrimary,

    scrim              = Color(0xCC000000)
)

@Composable
fun WiFiMotionTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
