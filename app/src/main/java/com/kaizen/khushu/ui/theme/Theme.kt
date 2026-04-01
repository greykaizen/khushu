package com.kaizen.khushu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun KhushuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    pureBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply Pure Black override if requested and in dark mode
    if (darkTheme && pureBlack) {
        colorScheme = colorScheme.copy(
            surface = androidx.compose.ui.graphics.Color.Black,
            background = androidx.compose.ui.graphics.Color.Black,
            surfaceContainer = androidx.compose.ui.graphics.Color.Black,
            surfaceContainerLow = androidx.compose.ui.graphics.Color.Black,
            surfaceContainerLowest = androidx.compose.ui.graphics.Color.Black,
            surfaceContainerHigh = androidx.compose.ui.graphics.Color.Black,
            surfaceContainerHighest = androidx.compose.ui.graphics.Color.Black,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}