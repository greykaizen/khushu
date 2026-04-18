package com.kaizen.khushu.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
    translationMap: Map<String, String> = emptyMap(),
    onBlockClick: (ContentBlock) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Standard horizontal padding for text-only blocks
    val textPadding = Modifier.padding(horizontal = 20.dp)
    
    when (block) {
        is HeadingBlock   -> HeadingBlockView(block, fg, modifier.then(textPadding).clickable { onBlockClick(block) })
        is ParagraphBlock -> ParagraphBlockView(block, settings, fg, modifier.then(textPadding))
        is AyahBlock      -> AyahBlockView(block, settings, fg, bg, translationMap, modifier.clickable { onBlockClick(block) })
        is HadithBlock    -> HadithBlockView(block, settings, fg, bg, modifier.clickable { onBlockClick(block) })
        is CalloutBlock   -> CalloutBlockView(block, fg, modifier.then(textPadding))
        is DividerBlock   -> DividerBlockView(fg, modifier.then(textPadding))
        is ArabicBlock    -> ArabicBlockView(block, settings, fg, bg, modifier.clickable { onBlockClick(block) })
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
            fontSize = 20.sp,
            lineHeight = 28.sp,
        ),
        color = fg,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
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
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}

// ── Ayah ───────────────────────────────────────────────────────────────────────

@Composable
private fun AyahBlockView(
    block: AyahBlock,
    settings: UserSettings,
    fg: Color,
    bg: Color,
    translationMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val arabicBg = when {
        bg == Color(0xFFF5E6C8) -> Color(0xFFEDD9A3) // paper tint
        bg == Color.White       -> Color(0xFFF0F0F0) // light tint
        else                    -> MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp), // Zero corners for full-bleed
        color = arabicBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Reference badge top-right
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AyahRefBadge(display = block.display, fg = fg)
            }

            Spacer(Modifier.height(24.dp))

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
                }
            }

            // Dynamic translation lookup
            val translationText = when {
                !settings.showTranslation -> null
                translationMap.isNotEmpty() -> com.kaizen.khushu.data.repository.TranslationRepository.getTranslation(translationMap, block.surah, block.ayah)
                    ?: block.translationEn
                else -> block.translationEn
            }

            // Determine if selected lang is RTL
            val isTranslationRtl = com.kaizen.khushu.data.model.AVAILABLE_TRANSLATIONS
                .find { it.id == settings.selectedTranslationLang }?.isRtl ?: false

            if (translationText != null) {
                Spacer(Modifier.height(16.dp))
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isTranslationRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    Text(
                        text = translationText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            fontSize = settings.translationSizeSp.sp,
                            lineHeight = (settings.translationSizeSp * 1.65f).sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = if (isTranslationRtl) TextAlign.Right else TextAlign.Center,
                        ),
                        color = fg.copy(alpha = 0.75f),
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
        shape = RoundedCornerShape(0.dp), // Full-bleed
        color = hadithBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Grade and Reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (block.grade != null) {
                    GradePill(grade = block.grade, fg = fg)
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                // Reference top-right
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

            Spacer(Modifier.height(8.dp)) // Extra space under header row

            // Arabic text
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
            }

            // Narrator (bottom-right)
            if (block.narrator != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "— ${block.narrator}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = fg.copy(alpha = 0.6f),
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
        else             -> fg.copy(alpha = 0.6f)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(pillColor.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = grade.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.1f))
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(IntrinsicSize.Max)
                .align(Alignment.CenterStart)
                .background(accentColor)
        )
        
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .padding(start = 4.dp), // Space for accent bar
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
        shape = RoundedCornerShape(0.dp), // Full-bleed
        color = arabicBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
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
