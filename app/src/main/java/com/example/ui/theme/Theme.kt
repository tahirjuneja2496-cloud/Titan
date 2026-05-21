package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,
    secondary = CharcoalDark,
    onSecondary = Color.White,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    background = MatteBlack,
    onBackground = Color.White,
    surface = DeepCharcoal,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = MutedText,
    error = Color(0xFFFF4D4D),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark theme by default for the ultimate gym aesthetic
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our brand colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
