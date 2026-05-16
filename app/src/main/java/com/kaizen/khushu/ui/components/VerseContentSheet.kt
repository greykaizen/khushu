package com.kaizen.khushu.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kaizen.khushu.data.model.ContentSource
import com.kaizen.khushu.data.model.ReflectionPost
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.ScheherazadeNew

/**
 * Unified bottom sheet for Tafsir + Reflections, matching Quran.com's side panel style.
 *
 * Header shows the referenced verse (Arabic + translation). Tabs switch between:
 *  - Tafsir: inline tafsir text (from TafsirRepository)
 *  - Reflections: live-fetched public posts from Quran Reflect
 *
 * Tabs are only visible if [source] supports the corresponding feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseContentSheet(
    surah: Int,
    ayah: Int,
    surahName: String,
    arabicText: String,
    translationText: String?,
    tafsirText: String?,
    isTafsirSource: Boolean,        // source.supportsTafsir
    reflections: List<ReflectionPost>,
    isReflectionsLoading: Boolean,
    isReflectionsSource: Boolean,   // source.supportsReflections
    initialTab: Int = 0,            // 0 = Tafsir, 1 = Reflections
    sheetState: SheetState,
    onDismiss: () -> Unit,
    arabicSizeSp: Float = 28f,
    translationSizeSp: Float = 16f,
) {
    // Build tab list dynamically based on source capabilities
    data class SheetTab(val label: String, val enabled: Boolean)
    val tabs = listOf(
        SheetTab("Tafsir", isTafsirSource),
        SheetTab("Reflections", isReflectionsSource),
    )
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, tabs.lastIndex)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ── Verse header ──────────────────────────────────────────────────
            item {
                VerseHeader(
                    surah = surah,
                    ayah = ayah,
                    surahName = surahName,
                    arabicText = arabicText,
                    translationText = translationText,
                    arabicSizeSp = arabicSizeSp,
                    translationSizeSp = translationSizeSp,
                )
            }

            // ── Tab bar ───────────────────────────────────────────────────────
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { if (tab.enabled) selectedTab = index },
                            enabled = tab.enabled,
                            text = {
                                Text(
                                    tab.label,
                                    fontFamily = BeVietnamPro,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = when {
                                        !tab.enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        selectedTab == index -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // ── Tab content ───────────────────────────────────────────────────
            item {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SheetTabContent"
                ) { tab ->
                    when (tab) {
                        0 -> TafsirContent(
                            tafsirText = tafsirText,
                            translationSizeSp = translationSizeSp,
                        )
                        1 -> ReflectionsContent(
                            reflections = reflections,
                            isLoading = isReflectionsLoading,
                        )
                    }
                }
            }

            // Bottom padding for nav bar
            item { Spacer(Modifier.height(120.dp)) }
        }
    }
}

// ── Verse header ────────────────────────────────────────────────────────────────

@Composable
private fun VerseHeader(
    surah: Int,
    ayah: Int,
    surahName: String,
    arabicText: String,
    translationText: String?,
    arabicSizeSp: Float,
    translationSizeSp: Float,
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        // Reference pill: "Al-Fatihah · 1:1"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "$surahName · $surah:$ayah",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = BeVietnamPro),
                    color = primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Arabic text
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = arabicText,
                fontFamily = ScheherazadeNew,
                fontSize = arabicSizeSp.sp,
                lineHeight = (arabicSizeSp * 1.75f).sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDirection = TextDirection.Rtl,
                    fontFamily = ScheherazadeNew,
                    textAlign = TextAlign.Start,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (translationText != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = translationText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = BeVietnamPro,
                    fontSize = translationSizeSp.sp,
                    lineHeight = (translationSizeSp * 1.65f).sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Tafsir content ──────────────────────────────────────────────────────────────

@Composable
private fun TafsirContent(
    tafsirText: String?,
    translationSizeSp: Float,
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp)) {
        if (tafsirText == null) {
            Text(
                "No tafsir loaded. Enable tafsir in settings to see explanations.",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BeVietnamPro),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Tafsir is Arabic (SPA5K) or English — detect by checking for Arabic chars
            val isArabic = tafsirText.any { it in '\u0600'..'\u06FF' }
            CompositionLocalProvider(
                LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                Text(
                    text = tafsirText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = if (isArabic) ScheherazadeNew else BeVietnamPro,
                        fontSize = if (isArabic) (translationSizeSp + 6f).sp else translationSizeSp.sp,
                        lineHeight = if (isArabic) ((translationSizeSp + 6f) * 1.8f).sp else (translationSizeSp * 1.65f).sp,
                        textAlign = if (isArabic) TextAlign.Right else TextAlign.Left,
                        textDirection = if (isArabic) TextDirection.Rtl else TextDirection.Ltr,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Reflections content ─────────────────────────────────────────────────────────

@Composable
private fun ReflectionsContent(
    reflections: List<ReflectionPost>,
    isLoading: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isLoading && reflections.isEmpty()) {
            // Skeleton shimmer placeholder rows
            repeat(3) {
                ReflectionSkeletonCard()
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        } else if (reflections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        "No reflections yet for this verse",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BeVietnamPro),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            reflections.forEach { post ->
                ReflectionCard(post = post)
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun ReflectionCard(post: ReflectionPost) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // User row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar placeholder — circle with initial
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.userName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        post.userName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (post.isVerified) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Verified",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (post.userHandle.isNotBlank()) {
                    Text(
                        "@${post.userHandle}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = BeVietnamPro),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        // Body
        Text(
            text = post.bodyPlain,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = BeVietnamPro,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )

        // Footer: likes + comments
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (post.likeCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                    Text(
                        post.likeCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = BeVietnamPro),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            if (post.reflectionCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.ModeComment,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                    Text(
                        post.reflectionCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = BeVietnamPro),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReflectionSkeletonCard() {
    val shimmerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(shimmerColor)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor)
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor)
        )
    }
}
