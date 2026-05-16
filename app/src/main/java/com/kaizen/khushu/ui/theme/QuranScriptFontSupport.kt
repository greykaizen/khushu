package com.kaizen.khushu.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import com.kaizen.khushu.data.repository.QuranScriptFontRepository

@Composable
fun rememberArabicScriptFontFamily(script: String, availableScripts: Set<String>): FontFamily {
    val context = LocalContext.current
    return remember(script, availableScripts) {
        when (script) {
            QuranScriptFontRepository.UTHMANIC_HAFS ->
                QuranScriptFontRepository.getDownloadedFontFamily(context, script) ?: ScheherazadeNew
            else -> ScheherazadeNew
        }
    }
}
