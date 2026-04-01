package com.kaizen.khushu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFontSpec
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.R

private val googleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val beVietnamProFont = GoogleFont("Be Vietnam Pro")

// Downloaded via GMS — cached after first load
val BeVietnamPro = FontFamily(
    GoogleFontSpec(googleFont = beVietnamProFont, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    GoogleFontSpec(googleFont = beVietnamProFont, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    GoogleFontSpec(googleFont = beVietnamProFont, fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    GoogleFontSpec(googleFont = beVietnamProFont, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
)

// Bundled in /res/font/antonio_regular.ttf
val Antonio = FontFamily(
    Font(resId = R.font.antonio_regular, weight = FontWeight.Normal),
)

// Fallback font in case Antonio doesn't load properly
val DisplayFontFallback = FontFamily.Default

val Typography = Typography(
    // Large display numbers — Salah picker, immersive counter
    displayLarge = TextStyle(
        fontFamily = Antonio,
        fontWeight = FontWeight.Normal,
        fontSize = 220.sp, // Restored to original Figma value
        lineHeight = 220.sp, // Restored to match font size
        letterSpacing = (-0.5).sp, // Slight negative spacing for tight look
    ),
    displayMedium = TextStyle(
        fontFamily = Antonio,
        fontWeight = FontWeight.Normal,
        fontSize = 100.sp, // Restored to original Figma value
        lineHeight = 100.sp, // Restored to match font size
        letterSpacing = 0.sp, // No letter spacing
    ),
    displaySmall = TextStyle(
        fontFamily = Antonio,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    // App bar titles and large headings
    headlineLarge = TextStyle(
        fontFamily = Antonio,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Antonio,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Antonio,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Antonio,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    // Section headers
    titleMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // Nav bar labels, buttons
    labelLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
