package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = Color(0xFF00363D),
  primaryContainer = Color(0xFF004F58),
  onPrimaryContainer = Color(0xFF97F0FF),
  secondary = AmberSecondary,
  onSecondary = Color(0xFF452B00),
  secondaryContainer = Color(0xFF633F00),
  onSecondaryContainer = Color(0xFFFFDF9E),
  tertiary = NeonGreen,
  onTertiary = Color(0xFF003915),
  background = DarkBackground,
  onBackground = DarkOnBackground,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = DarkOutline,
  error = SignalRed
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF006876),
  onPrimary = Color.White,
  primaryContainer = Color(0xFF9CEFFF),
  onPrimaryContainer = Color(0xFF001F24),
  secondary = Color(0xFF825500),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFDDAE),
  onSecondaryContainer = Color(0xFF2A1800),
  tertiary = Color(0xFF006E31),
  onTertiary = Color.White,
  background = LightBackground,
  onBackground = LightOnBackground,
  surface = LightSurface,
  onSurface = LightOnSurface,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = Color(0xFF475569),
  outline = LightOutline,
  error = Color(0xFFBA1A1A)
)

@Composable
fun NXTTVTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
