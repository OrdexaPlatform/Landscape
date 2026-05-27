package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PineGreenPrimaryDark,
    secondary = MintSecondaryDark,
    tertiary = SandTertiaryDark,
    background = DarkPineBackground,
    surface = DarkPineSurface,
    onPrimary = DarkPineBackground,
    onSecondary = DarkPineBackground,
    onBackground = LightTextDark,
    onSurface = LightTextDark,
    error = ErrorRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ForestGreenPrimary,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekOnPrimaryContainer,
    secondary = SageGreenSecondary,
    secondaryContainer = SleekSecondaryContainer,
    tertiary = EarthBrownTertiary,
    background = SoftCreamBackground,
    surface = CardSurfaceLight,
    onPrimary = CardSurfaceLight,
    onSecondary = CardSurfaceLight,
    onBackground = DeepTextDark,
    onSurface = DeepTextDark,
    outline = SleekBorder,
    error = ErrorRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color disables custom styling on user devices. We set to false to honor custom Sleek theme.
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
