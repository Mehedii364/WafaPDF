package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  SYSTEM, LIGHT, DARK, SEPIA
}

private val DarkColorScheme = darkColorScheme(
  primary = CrimsonLight,
  onPrimary = Color.White,
  primaryContainer = CrimsonDark,
  onPrimaryContainer = Color.White,
  secondary = Slate400,
  onSecondary = Color.Black,
  background = Slate900,
  onBackground = Color(0xFFF1F5F9),
  surface = Slate800,
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Slate700,
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Slate600
)

private val LightColorScheme = lightColorScheme(
  primary = CrimsonPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFEBEE),
  onPrimaryContainer = CrimsonDark,
  secondary = Slate700,
  onSecondary = Color.White,
  background = Slate50,
  onBackground = Slate900,
  surface = Color.White,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate700,
  outline = Slate200
)

private val SepiaColorScheme = lightColorScheme(
  primary = SepiaPrimary,
  onPrimary = Color.White,
  primaryContainer = SepiaSurfaceVariant,
  onPrimaryContainer = SepiaOnSurface,
  secondary = SepiaSecondary,
  onSecondary = Color.White,
  background = SepiaBackground,
  onBackground = SepiaOnSurface,
  surface = SepiaSurface,
  onSurface = SepiaOnSurface,
  surfaceVariant = SepiaSurfaceVariant,
  onSurfaceVariant = SepiaSecondary,
  outline = Color(0xFFDEC5A2)
)

@Composable
fun WafaPdfTheme(
  themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  content: @Composable () -> Unit,
) {
  val isDark = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    AppThemeMode.DARK -> true
    AppThemeMode.LIGHT -> false
    AppThemeMode.SEPIA -> false
  }

  val colorScheme = when {
    themeMode == AppThemeMode.SEPIA -> SepiaColorScheme
    isDark -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
