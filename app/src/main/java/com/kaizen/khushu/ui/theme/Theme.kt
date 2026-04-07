package com.kaizen.khushu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.dynamicColorScheme

val colorSeeds = listOf(
    "default" to Color(0xFF6650A4),
    "teal"    to Color(0xFF00695C),
    "green"   to Color(0xFF2E7D32),
    "amber"   to Color(0xFFB45309),
    "red"     to Color(0xFFC62828),
    "blue"    to Color(0xFF1565C0),
    "pink"    to Color(0xFFAD1457),
    "slate"   to Color(0xFF37474F),
)

fun colorFromSeed(seed: String): Color =
    colorSeeds.firstOrNull { it.first == seed }?.second ?: Color(0xFF6650A4)

@Composable
fun KhushuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    pureBlack: Boolean = false,
    colorSeed: String = "default",
    content: @Composable () -> Unit
) {
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> dynamicColorScheme(
            seedColor = colorFromSeed(colorSeed),
            isDark = darkTheme,
            isAmoled = false
        )
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