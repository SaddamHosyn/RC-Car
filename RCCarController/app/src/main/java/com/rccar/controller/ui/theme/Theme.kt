package com.rccar.controller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary          = PrimaryDark,
    onPrimary        = OnPrimaryDark,
    primaryContainer = NeonCyanDim,
    secondary        = SecondaryDark,
    tertiary         = TertiaryDark,
    background       = BackgroundDark,
    surface          = SurfaceDark,
    surfaceVariant   = SurfaceVariantDark,
    onSurface        = OnSurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline          = CardBorder,
    error            = Danger,
)

/**
 * Root theme composable.
 * The app is always dark — it's a device controller; OLED-friendly is a feature, not a bug.
 */
@Composable
fun RCCarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = AppTypography,
        content     = content,
    )
}
