package com.kaizen.khushu.ui.screens.learn

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.kaizen.khushu.ui.theme.ScheherazadeNew

/**
 * Renders Arabic text with tajweed color rules applied.
 *
 * Markup format: {rule}text{/rule}
 * Supported rules and their colors:
 *   madd      → gold   — vowel prolongation (2–6 counts)
 *   ghunna    → green  — nasalization (noon/meem with shaddah)
 *   qalqalah  → blue   — echo (ق ط ب ج د when saakin)
 *   ikhfa     → gray   — concealment
 *   idgham    → orange — assimilation
 *
 * Untagged text renders in the provided [color].
 */
@Composable
fun TajweedText(
    markup: String,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val annotated = remember(markup, color) { parseTajweed(markup, color) }
    Text(
        text = annotated,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontFamily = ScheherazadeNew,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge.copy(
            textDirection = TextDirection.Rtl,
            fontFamily = ScheherazadeNew,
        ),
        modifier = modifier,
    )
}

private fun parseTajweed(markup: String, defaultColor: Color) = buildAnnotatedString {
    val tagRegex = Regex("""\{(\w+)\}(.*?)\{/\1\}""", RegexOption.DOT_MATCHES_ALL)
    var cursor = 0
    for (match in tagRegex.findAll(markup)) {
        if (match.range.first > cursor) {
            withStyle(SpanStyle(color = defaultColor)) {
                append(markup.substring(cursor, match.range.first))
            }
        }
        withStyle(SpanStyle(color = tajweedColor(match.groupValues[1], defaultColor))) {
            append(match.groupValues[2])
        }
        cursor = match.range.last + 1
    }
    if (cursor < markup.length) {
        withStyle(SpanStyle(color = defaultColor)) {
            append(markup.substring(cursor))
        }
    }
}

private fun tajweedColor(rule: String, default: Color): Color = when (rule) {
    "madd"     -> Color(0xFFFF5252) // Vibrant Red — prolongation
    "ghunna"   -> Color(0xFF4CAF50) // Green — nasalization
    "qalqalah" -> Color(0xFF40C4FF) // Light Blue — echo
    "ikhfa"    -> Color(0xFF9E9E9E) // Gray — concealment
    "idgham"   -> Color(0xFFFF9800) // Orange — assimilation
    else       -> default
}
