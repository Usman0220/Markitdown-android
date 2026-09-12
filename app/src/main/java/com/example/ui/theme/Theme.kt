package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = Sky400,
  onPrimary = Slate950,
  primaryContainer = Sky600,
  onPrimaryContainer = Color.White,
  secondary = Indigo400,
  onSecondary = Slate950,
  secondaryContainer = Indigo600,
  onSecondaryContainer = Color.White,
  tertiary = Emerald400,
  onTertiary = Slate950,
  background = Slate950,
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate300,
  outline = Slate700,
  outlineVariant = Slate800
)

private val LightColorScheme = lightColorScheme(
  primary = Sky600,
  onPrimary = Color.White,
  primaryContainer = Sky300.copy(alpha = 0.3f),
  onPrimaryContainer = Sky600,
  secondary = Indigo600,
  onSecondary = Color.White,
  secondaryContainer = Indigo400.copy(alpha = 0.2f),
  onSecondaryContainer = Indigo600,
  tertiary = Emerald600,
  onTertiary = Color.White,
  background = Slate50,
  onBackground = Slate900,
  surface = Color.White,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate600,
  outline = Slate300,
  outlineVariant = Slate200
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our handcrafted developer theme by default for consistency
  content: @Composable () -> Unit,
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

