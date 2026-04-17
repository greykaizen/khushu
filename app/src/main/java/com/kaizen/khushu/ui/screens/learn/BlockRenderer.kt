package com.kaizen.khushu.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.model.ArabicBlock
import com.kaizen.khushu.data.model.AyahBlock
import com.kaizen.khushu.data.model.CalloutBlock
import com.kaizen.khushu.data.model.ContentBlock
import com.kaizen.khushu.data.model.DividerBlock
import com.kaizen.khushu.data.model.HadithBlock
import com.kaizen.khushu.data.model.HeadingBlock
import com.kaizen.khushu.data.model.ParagraphBlock
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.ScheherazadeNew

/**
 * Renders a single [ContentBlock] into Compose UI.
 *
 * [fg] and [bg] come from the reading screen's theme (dark / paper / light).
 * [settings] provides Arabic text size and tajweed preference.
 */
@Composable
fun BlockRenderer(
    block: ContentBlock,
    settings: UserSettings,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    when (block) {
        is HeadingBlock  -> HeadingBlockView(block, fg, modifier)
        is ParagraphBlock -> ParagraphBlockView(block, settings, fg, modifier)
        is AyahBlock     -> AyahBlockView(block, settings, fg, bg, modifier)
        is HadithBlock   -> HadithBlockView(block, settings, fg, bg, modifier)
        is CalloutBlock  -> CalloutBlockView(block, fg, modifier)
        is DividerBlock  -> DividerBlockView(fg, modifier)
        is ArabicBlock   -> ArabicBlockView(block, settings, fg, bg, modifier)
    }
}

// ── Heading ────────────────────────────────────────────────────────────────────

@Composable
private fun HeadingBlockView(block: HeadingBlock, fg: Color, modifier: Modifier = Modifier) {
    Text(
        text = block.text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.SemiBold,
        ),
        color = fg,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
    )
}

// ── Paragraph ──────────────────────────────────────────────────────────────────

@Composable
private fun ParagraphBlockView(
    block: ParagraphBlock,
    settings: UserSettings,
    fg: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = block.text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = BeVietnamPro,
            fontSize = settings.translationSizeSp.sp,
            lineHeight = (settings.translationSizeSp * 1.65f).sp,
        ),
        color = fg.copy(alpha = 0.85f),
        modifier = modifier.fillMaxWidth(),
    )
}

// ── Ayah ───────────────────────────────────────────────────────────────────────

@Composable
private fun AyahBlockView(
    block: AyahBlock,
    settings: UserSettings,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    val arabicBg = when {
        bg == Color(0xFFF5E6C8) -> Color(0xFFEDD9A3) // paper tint
        bg == Color.White       -> Color(0xFFF0F0F0) // light tint
        else                    -> MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = arabicBg,
        shadowElevation = 4.dp,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Reference badge top-right
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AyahRefBadge(display = block.display, fg = fg)
            }

            Spacer(Modifier.height(12.dp))

            // Arabic text — prefer tajweed markup when enabled
            val useMarkup = settings.showTajweed && block.tajweedMarkup != null
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                if (useMarkup) {
                    TajweedText(
                        markup = block.tajweedMarkup!!,
                        fontSize = settings.arabicSizeSp.sp,
                        lineHeight = (settings.arabicSizeSp * 1.75f).sp,
                        color = fg,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else if (block.textUthmani != null) {
                    Text(
                        text = block.textUthmani,
                        fontFamily = ScheherazadeNew,
                        fontSize = settings.arabicSizeSp.sp,
                        lineHeight = (settings.arabicSizeSp * 1.75f).sp,
                        textAlign = TextAlign.Center,
                        color = fg,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDirection = TextDirection.Rtl,
                            fontFamily = ScheherazadeNew,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    // Fallback: show display reference while DB entry loads
                    Text(
                        text = block.display,
                        fontFamily = BeVietnamPro,
                        fontSize = 14.sp,
                        color = fg.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AyahRefBadge(display: String, fg: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = fg.copy(alpha = 0.08f),
    ) {
        Text(
            text = display,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
            color = fg.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

// ── Hadith ─────────────────────────────────────────────────────────────────────

@Composable
private fun HadithBlockView(
    block: HadithBlock,
    settings: UserSettings,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    val hadithBg = when {
        bg == Color(0xFFF5E6C8) -> Color(0xFFEDD9A3)
        bg == Color.White       -> Color(0xFFF0F0F0)
        else                    -> MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = hadithBg,
        shadowElevation = 4.dp,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Quote icon header
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = fg.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp),
            )

            // Arabic text (nullable — may not be in DB)
            if (block.textArabic != null) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = block.textArabic,
                        fontFamily = ScheherazadeNew,
                        fontSize = settings.arabicSizeSp.sp,
                        lineHeight = (settings.arabicSizeSp * 1.75f).sp,
                        textAlign = TextAlign.Center,
                        color = fg,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDirection = TextDirection.Rtl,
                            fontFamily = ScheherazadeNew,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // English text
            val bodyText = block.textEn
            if (bodyText != null) {
                Text(
                    text = "\u201C$bodyText\u201D",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = BeVietnamPro,
                        fontSize = settings.translationSizeSp.sp,
                        lineHeight = (settings.translationSizeSp * 1.65f).sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = fg.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                // No DB entry yet — show reference as placeholder
                Text(
                    text = block.display,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BeVietnamPro),
                    color = fg.copy(alpha = 0.5f),
                )
            }

            // Grade + narrator line
            if (block.grade != null || block.narrator != null) {
                HorizontalDivider(
                    color = fg.copy(alpha = 0.1f),
                    thickness = 1.dp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (block.grade != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GradePill(grade = block.grade, fg = fg)
                        }
                    }
                    if (block.narrator != null) {
                        Text(
                            text = "Narrated by ${block.narrator}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = BeVietnamPro,
                            ),
                            color = fg.copy(alpha = 0.55f),
                        )
                    }
                }
            }

            // Source reference (bottom-right)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = fg.copy(alpha = 0.06f),
                ) {
                    Text(
                        text = block.display,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = fg.copy(alpha = 0.45f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GradePill(grade: String, fg: Color) {
    val pillColor = when (grade.lowercase()) {
        "sahih", "صحيح" -> Color(0xFF2E7D32) // Green for authentic
        "hasan", "حسن"  -> Color(0xFF1565C0) // Blue for good
        else             -> fg.copy(alpha = 0.4f)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(pillColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = grade,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.SemiBold,
            ),
            color = pillColor,
        )
    }
}

// ── Callout ────────────────────────────────────────────────────────────────────

@Composable
private fun CalloutBlockView(
    block: CalloutBlock,
    fg: Color,
    modifier: Modifier = Modifier,
) {
    val (accentColor, icon) = when (block.style) {
        "tip"     -> MaterialTheme.colorScheme.primary to Icons.Default.Lightbulb
        "warning" -> Color(0xFFF59E0B) to Icons.Default.Info
        else      -> MaterialTheme.colorScheme.secondary to Icons.Default.Info // "note"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
        )
        Text(
            text = block.text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = BeVietnamPro,
                lineHeight = 22.sp,
            ),
            color = fg.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Divider ────────────────────────────────────────────────────────────────────

@Composable
private fun DividerBlockView(fg: Color, modifier: Modifier = Modifier) {
    HorizontalDivider(
        color = fg.copy(alpha = 0.08f),
        thickness = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
}

// ── Arabic (standalone dua / phrase) ──────────────────────────────────────────

@Composable
private fun ArabicBlockView(
    block: ArabicBlock,
    settings: UserSettings,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    val arabicBg = when {
        bg == Color(0xFFF5E6C8) -> Color(0xFFEDD9A3)
        bg == Color.White       -> Color(0xFFF0F0F0)
        else                    -> MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = arabicBg,
        shadowElevation = 4.dp,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                if (block.tajweed != null && settings.showTajweed) {
                    TajweedText(
                        markup = block.tajweed,
                        fontSize = settings.arabicSizeSp.sp,
                        lineHeight = (settings.arabicSizeSp * 1.75f).sp,
                        color = fg,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = block.text,
                        fontFamily = ScheherazadeNew,
                        fontSize = settings.arabicSizeSp.sp,
                        lineHeight = (settings.arabicSizeSp * 1.75f).sp,
                        color = fg,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDirection = TextDirection.Rtl,
                            fontFamily = ScheherazadeNew,
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (block.translation.isNotBlank()) {
                Text(
                    text = block.translation,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = BeVietnamPro,
                        fontSize = settings.translationSizeSp.sp,
                        lineHeight = (settings.translationSizeSp * 1.65f).sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                    ),
                    color = fg.copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
