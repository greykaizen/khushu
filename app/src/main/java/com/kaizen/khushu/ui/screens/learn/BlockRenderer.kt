package com.kaizen.khushu.ui.screens.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
import com.kaizen.khushu.data.model.ContentSource
import com.kaizen.khushu.data.model.DividerBlock
import com.kaizen.khushu.data.model.HadithBlock
import com.kaizen.khushu.data.model.HeadingBlock
import com.kaizen.khushu.data.model.ParagraphBlock
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.ui.components.AyahEndMarker
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
    tajweedMap: Map<String, String> = emptyMap(),
    scriptMap: Map<String, String> = emptyMap(),
    arabicFontFamily: FontFamily = ScheherazadeNew,
    isHighlighted: Boolean = false,
    onBlockClick: (ContentBlock) -> Unit = {},
    onPlayClick: ((ContentBlock) -> Unit)? = null,
    onBookmarkClick: ((ContentBlock) -> Unit)? = null,
    onTafsirClick: ((ContentBlock) -> Unit)? = null,
    onReflectionsClick: ((ContentBlock) -> Unit)? = null,
    source: ContentSource = ContentSource.QF,
    readingMode: String = "verse_by_verse",
    modifier: Modifier = Modifier,
) {
    val textPadding = Modifier.padding(horizontal = 20.dp)

    when (block) {
        is HeadingBlock   -> HeadingBlockView(block, fg, modifier.then(textPadding).clickable { onBlockClick(block) })
        is ParagraphBlock -> ParagraphBlockView(block, settings, fg, modifier.then(textPadding))
        is AyahBlock      -> AyahBlockView(
            block = block,
            settings = settings,
            fg = fg,
            bg = bg,
            translationMap = translationMap,
            tajweedMap = tajweedMap,
            scriptMap = scriptMap,
            arabicFontFamily = arabicFontFamily,
            isHighlighted = isHighlighted,
            readingMode = readingMode,
            source = source,
            onPlayClick = { onPlayClick?.invoke(block) },
            onBookmarkClick = { onBookmarkClick?.invoke(block) },
            onTafsirClick = { onTafsirClick?.invoke(block) },
            onReflectionsClick = { onReflectionsClick?.invoke(block) },
            modifier = modifier.clickable { onBlockClick(block) }
        )
        is HadithBlock    -> HadithBlockView(block, settings, fg, bg, modifier.clickable { onBlockClick(block) })
        is CalloutBlock   -> CalloutBlockView(block, fg, modifier.then(textPadding))
        is DividerBlock   -> DividerBlockView(fg, modifier.then(textPadding))
        is ArabicBlock    -> ArabicBlockView(block, settings, fg, bg, arabicFontFamily, modifier.clickable { onBlockClick(block) })
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

// ── Ayah (Quran.com style) ─────────────────────────────────────────────────────

@Composable
private fun AyahBlockView(
    block: AyahBlock,
    settings: UserSettings,
    fg: Color,
    bg: Color,
    translationMap: Map<String, String>,
    tajweedMap: Map<String, String>,
    scriptMap: Map<String, String>,
    arabicFontFamily: FontFamily,
    isHighlighted: Boolean,
    readingMode: String,
    source: ContentSource = ContentSource.QF,
    onPlayClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onTafsirClick: () -> Unit,
    onReflectionsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isReadingMode = readingMode == "reading"
    val highlightBg = if (isHighlighted)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    else Color.Transparent

    val contentColor = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer else fg
    val primaryColor = MaterialTheme.colorScheme.primary

    // Reading mode: seamless Arabic flow, no chrome
    if (isReadingMode) {
        ReadingModeAyah(block, settings, contentColor, fg, tajweedMap, scriptMap, arabicFontFamily, inlineContent = remember(block.ayah, contentColor) {
            mapOf(
                "marker" to androidx.compose.foundation.text.InlineTextContent(
                    androidx.compose.ui.text.Placeholder(
                        width = 38.sp,
                        height = 32.sp,
                        placeholderVerticalAlign = androidx.compose.ui.text.PlaceholderVerticalAlign.Center
                    )
                ) { AyahEndMarker(number = block.ayah, fg = contentColor) }
            )
        }, modifier = modifier.background(highlightBg))
        return
    }

    // Inline ayah-end marker
    val inlineContent = remember(block.ayah, contentColor) {
        mapOf(
            "marker" to androidx.compose.foundation.text.InlineTextContent(
                androidx.compose.ui.text.Placeholder(
                    width = 38.sp,
                    height = 32.sp,
                    placeholderVerticalAlign = androidx.compose.ui.text.PlaceholderVerticalAlign.Center
                )
            ) {
                AyahEndMarker(number = block.ayah, fg = contentColor)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(highlightBg)
    ) {
        // ── Top: verse number badge only ─────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 2.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.10f),
                        radius = size.minDimension / 2f
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.35f),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 1.4f)
                    )
                }
                Text(
                    text = block.ayah.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = primaryColor,
                )
            }
        }

        // ── Arabic text ───────────────────────────────────────────────────────
        val verseKey = "${block.surah}:${block.ayah}"
        val tajweedMarkup = if (settings.selectedScript == "uthmani")
            tajweedMap[verseKey] ?: block.tajweedMarkup
        else null

        val plainArabicText = if (settings.selectedScript != "uthmani")
            scriptMap[verseKey] ?: block.textUthmani
        else block.textUthmani

        val showTajweed = settings.showTajweed && tajweedMarkup != null

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (showTajweed) {
                TajweedText(
                    markup = tajweedMarkup!!,
                    fontSize = settings.arabicSizeSp.sp,
                    lineHeight = (settings.arabicSizeSp * 1.8f).sp,
                    color = contentColor,
                    inlineContent = inlineContent,
                    appendMarker = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 6.dp, bottom = 4.dp)
                )
            } else if (plainArabicText != null) {
                val annotatedString = remember(plainArabicText, contentColor) {
                    buildAnnotatedString {
                        append(plainArabicText)
                        appendInlineContent("marker", "[marker]")
                    }
                }
                Text(
                    text = annotatedString,
                    fontFamily = arabicFontFamily,
                    fontSize = settings.arabicSizeSp.sp,
                    lineHeight = (settings.arabicSizeSp * 1.8f).sp,
                    textAlign = TextAlign.Start,
                    color = contentColor,
                    inlineContent = inlineContent,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDirection = TextDirection.Rtl,
                        fontFamily = arabicFontFamily,
                        color = contentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 6.dp, bottom = 4.dp),
                )
            }
        }

        // ── Translation ───────────────────────────────────────────────────────
        val translationText = when {
            !settings.showTranslation -> null
            translationMap.isNotEmpty() -> com.kaizen.khushu.data.repository.TranslationRepository
                .getTranslation(translationMap, block.surah, block.ayah) ?: block.translationEn
            else -> block.translationEn
        }
        val isTranslationRtl = com.kaizen.khushu.data.model.TranslationMeta.AVAILABLE_TRANSLATIONS
            .find { it.id == settings.selectedTranslationLang }?.isRtl ?: false

        if (translationText != null) {
            CompositionLocalProvider(
                LocalLayoutDirection provides if (isTranslationRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                Text(
                    text = translationText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = BeVietnamPro,
                        fontSize = settings.translationSizeSp.sp,
                        lineHeight = (settings.translationSizeSp * 1.7f).sp,
                        textAlign = if (isTranslationRtl) TextAlign.Right else TextAlign.Left,
                    ),
                    color = contentColor.copy(alpha = 0.78f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 2.dp, bottom = 8.dp),
                )
            }
        }

        // ── Inline Tafsir ─────────────────────────────────────────────────────
        if (settings.showTafsir && block.tafsirText != null) {
            val isSpa5k = settings.selectedTafsirSource == "SPA5K"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Tafsir",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = BeVietnamPro),
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = block.tafsirText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = if (isSpa5k) ScheherazadeNew else BeVietnamPro,
                                fontSize = if (isSpa5k) (settings.translationSizeSp + 5f).sp else settings.translationSizeSp.sp,
                                lineHeight = if (isSpa5k) ((settings.translationSizeSp + 5f) * 1.8f).sp else (settings.translationSizeSp * 1.6f).sp,
                                textAlign = TextAlign.Right,
                            ),
                            color = contentColor.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        // ── Bottom row: chips (left) + action icons (right) ──────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: content chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tafsir chip — hide when inline tafsir already expanded
                if (source.supportsTafsir && (!settings.showTafsir || block.tafsirText == null)) {
                    VerseChip(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        label = "Tafsir",
                        contentColor = contentColor,
                        onClick = onTafsirClick
                    )
                }
                // Reflections chip — only visible if source supports it
                if (source.supportsReflections) {
                    VerseChip(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        label = "Reflections",
                        contentColor = contentColor,
                        onClick = onReflectionsClick
                    )
                }
            }

            // Right: action icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (source.supportsAudio) {
                    IconButton(onClick = onPlayClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = contentColor.copy(alpha = 0.45f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                IconButton(onClick = onBookmarkClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = contentColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = contentColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = contentColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // ── Verse separator ───────────────────────────────────────────────────
        HorizontalDivider(
            color = contentColor.copy(alpha = 0.08f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Reading mode: pure Arabic text flow, no action bar, no translation, no chips.
 * Uses a slightly larger line-height and justified/RTL rendering.
 * Font: ScheherazadeNew at the user's selected arabicSizeSp.
 */
@Composable
private fun ReadingModeAyah(
    block: AyahBlock,
    settings: UserSettings,
    contentColor: Color,
    fg: Color,
    tajweedMap: Map<String, String>,
    scriptMap: Map<String, String>,
    arabicFontFamily: FontFamily,
    inlineContent: Map<String, androidx.compose.foundation.text.InlineTextContent>,
    modifier: Modifier = Modifier,
) {
    val verseKey = "${block.surah}:${block.ayah}"
    val tajweedMarkup = if (settings.selectedScript == "uthmani")
        tajweedMap[verseKey] ?: block.tajweedMarkup
    else null
    val plainArabicText = if (settings.selectedScript != "uthmani")
        scriptMap[verseKey] ?: block.textUthmani
    else block.textUthmani
    val showTajweed = settings.showTajweed && tajweedMarkup != null

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (showTajweed) {
            TajweedText(
                markup = tajweedMarkup!!,
                fontSize = settings.arabicSizeSp.sp,
                lineHeight = (settings.arabicSizeSp * 2.0f).sp,
                color = contentColor,
                inlineContent = inlineContent,
                appendMarker = true,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 2.dp)
            )
        } else if (plainArabicText != null) {
            val annotatedString = remember(plainArabicText) {
                buildAnnotatedString {
                    append(plainArabicText)
                    appendInlineContent("marker", "[marker]")
                }
            }
            Text(
                text = annotatedString,
                fontFamily = arabicFontFamily,
                fontSize = settings.arabicSizeSp.sp,
                lineHeight = (settings.arabicSizeSp * 2.0f).sp,
                // No inter-verse gap — seamless reading flow
                color = contentColor,
                inlineContent = inlineContent,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDirection = TextDirection.Rtl,
                    fontFamily = arabicFontFamily,
                    textAlign = TextAlign.Justify,
                    color = contentColor
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun VerseChip(
    icon: ImageVector,
    label: String,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = contentColor.copy(alpha = 0.07f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor.copy(alpha = 0.5f)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = BeVietnamPro),
                color = contentColor.copy(alpha = 0.6f)
            )
        }
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
        shape = RoundedCornerShape(0.dp),
        color = hadithBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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

            Spacer(Modifier.height(8.dp))

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
        "sahih", "صحيح" -> Color(0xFF2E7D32)
        "hasan", "حسن"  -> Color(0xFF1565C0)
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
        else      -> MaterialTheme.colorScheme.secondary to Icons.Default.Info
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.1f))
    ) {
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
                .padding(start = 4.dp),
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

// ── Arabic standalone ──────────────────────────────────────────────────────────

@Composable
private fun ArabicBlockView(
    block: ArabicBlock,
    settings: UserSettings,
    fg: Color,
    bg: Color,
    arabicFontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    val arabicBg = when {
        bg == Color(0xFFF5E6C8) -> Color(0xFFEDD9A3)
        bg == Color.White       -> Color(0xFFF0F0F0)
        else                    -> MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
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
                        fontFamily = arabicFontFamily,
                        fontSize = settings.arabicSizeSp.sp,
                        lineHeight = (settings.arabicSizeSp * 1.75f).sp,
                        color = fg,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDirection = TextDirection.Rtl,
                            fontFamily = arabicFontFamily,
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
