package com.kaizen.khushu.ui.screens.learn

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.model.LearnTopic
import com.kaizen.khushu.data.model.WordData
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.ScheherazadeNew

// ── Theme helpers ──────────────────────────────────────────────────────────────

private val ThemeDark = Color.Black
private val ThemePaper = Color(0xFFF5E6C8)
private val ThemeLight = Color.White

private fun bgColor(theme: String) = when (theme) {
    "PAPER" -> ThemePaper
    "LIGHT" -> ThemeLight
    else -> ThemeDark
}

private fun contentColor(theme: String) = when (theme) {
    "PAPER", "LIGHT" -> Color.Black
    else -> Color.White
}

private fun surfaceColor(theme: String) = when (theme) {
    "PAPER" -> Color(0xFFEDD9A3)
    "LIGHT" -> Color(0xFFF0F0F0)
    else -> Color(0xFF1A1A1A)
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnReadingScreen(
    topic: LearnTopic,
    settingsViewModel: SettingsViewModel,
    learnAudioViewModel: LearnAudioViewModel,
    onBack: () -> Unit,
    initialAyahIndex: Int? = null,
    modifier: Modifier = Modifier,
) {
    val settings by settingsViewModel.settings.collectAsState()
    val audioState by learnAudioViewModel.audioState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var showSettings by remember { mutableStateOf(false) }
    var showActionSheet by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val bg = bgColor(settings.readingTheme)
    val fg = contentColor(settings.readingTheme)

    val titleFraction = scrollBehavior.state.collapsedFraction
    val titleFontSize = androidx.compose.ui.util.lerp(28f, 20f, titleFraction).sp

    // Track last read topic
    androidx.compose.runtime.LaunchedEffect(topic.id) {
        settingsViewModel.updateLastReadTopicId(topic.id)
    }

    // Scroll to initial index if provided
    androidx.compose.runtime.LaunchedEffect(initialAyahIndex) {
        if (initialAyahIndex != null) {
            listState.animateScrollToItem(initialAyahIndex)
        }
    }

    // WakeLock — hold screen on while reading if pref is set
    val activity = LocalActivity.current
    DisposableEffect(settings.readingKeepScreenOn) {
        if (settings.readingKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = bg,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = topic.title,
                        fontFamily = BeVietnamPro,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Normal,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = fg,
                        )
                    }
                },
                actions = {
                    if (topic.audioFilename != null) {
                        IconButton(onClick = {
                            when (audioState) {
                                is LearnAudioViewModel.AudioState.Playing -> learnAudioViewModel.pause()
                                is LearnAudioViewModel.AudioState.Loading -> { /* do nothing */ }
                                else -> learnAudioViewModel.play(topic)
                            }
                        }) {
                            when (audioState) {
                                is LearnAudioViewModel.AudioState.Playing -> {
                                    Icon(
                                        Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = fg.copy(alpha = 0.7f),
                                    )
                                }
                                is LearnAudioViewModel.AudioState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = fg.copy(alpha = 0.7f),
                                    )
                                }
                                else -> {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = fg.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Reading settings",
                            tint = fg.copy(alpha = 0.7f),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = bg,
                    scrolledContainerColor = bg,
                    titleContentColor = fg,
                    navigationIconContentColor = fg,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                AyatBlock(
                    topic = topic,
                    settings = settings,
                    fg = fg,
                    bg = bg,
                    onTap = { showActionSheet = true }
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        if (showActionSheet) {
            val isBookmarked = settings.bookmarkedAyahs.contains("${topic.id}:0")
            val isMastered = settings.masteredTopicIds.contains(topic.id)

            AyahActionSheet(
                onDismiss = { showActionSheet = false },
                onPlay = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    learnAudioViewModel.play(topic)
                    showActionSheet = false
                },
                onBookmark = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    settingsViewModel.toggleBookmark(topic.id, 0)
                    val msg = if (isBookmarked) "Bookmark removed" else "Bookmark added"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    showActionSheet = false
                },
                onMastered = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    settingsViewModel.toggleMastered(topic.id)
                    val msg = if (isMastered) "Marked as incomplete" else "Marked as mastered"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    showActionSheet = false
                },
                onShare = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Sharing feature coming soon!", Toast.LENGTH_SHORT).show()
                    showActionSheet = false
                },
                isBookmarked = isBookmarked,
                isMastered = isMastered
            )
        }

        if (showSettings) {
            ReadingSettingsSheet(
                topic = topic,
                settings = settings,
                onDismiss = { showSettings = false },
                onThemeChange = { settingsViewModel.setReadingTheme(it) },
                onArabicSizeChange = { settingsViewModel.setArabicSizeSp(it) },
                onTranslationSizeChange = { settingsViewModel.setTranslationSizeSp(it) },
                onShowTranslationChange = { settingsViewModel.toggleShowTranslation(it) },
                onShowTransliterationChange = { settingsViewModel.toggleShowTransliteration(it) },
                onShowWordByWordChange = { settingsViewModel.toggleShowWordByWord(it) },
                onKeepScreenOnChange = { settingsViewModel.toggleReadingKeepScreenOn(it) },
                onShowTajweedChange = { settingsViewModel.toggleShowTajweed(it) },
            )
        }
    }
}

// ── Arabic + Translation block ─────────────────────────────────────────────────

@Composable
private fun AyatBlock(
    topic: LearnTopic,
    settings: UserSettings,
    fg: Color,
    bg: Color,
    onTap: () -> Unit,
) {
    val dividerColor = fg.copy(alpha = 0.12f)
    
    // Use surfaceContainer for better elevation support
    val arabicBg = when (settings.readingTheme) {
        "PAPER" -> Color(0xFFEDD9A3)
        "LIGHT" -> Color(0xFFF0F0F0)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Arabic text block with elevation - Full Width with small padding to show corners
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = arabicBg,
            shadowElevation = 8.dp,
            tonalElevation = 8.dp,
            onClick = onTap
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Reference at top right
                if (topic.referenceSource != null || topic.referenceNumber != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ReferenceBadge(
                            source = topic.referenceSource,
                            number = topic.referenceNumber,
                            fg = fg,
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                if (settings.showTajweed && topic.tajweedMarkup != null) {
                    TajweedText(
                        markup = topic.tajweedMarkup,
                        fontSize = settings.arabicSizeSp.sp,
                        lineHeight = (settings.arabicSizeSp * 1.75f).sp,
                        color = fg,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = topic.arabicText,
                        fontSize = settings.arabicSizeSp.sp,
                        lineHeight = (settings.arabicSizeSp * 1.75f).sp,
                        fontFamily = ScheherazadeNew,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = fg,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDirection = TextDirection.Rtl,
                            fontFamily = ScheherazadeNew,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Word-by-word row
        if (settings.showWordByWord && topic.words.isNotEmpty()) {
            Box(modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            )) {
                WordRow(
                    words = topic.words,
                    fg = fg,
                    bg = bg,
                    showTransliteration = settings.showTransliteration,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // Translation
        val translation = topic.translations["en"]
        if (settings.showTranslation && translation != null) {
            Text(
                text = translation,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = BeVietnamPro,
                    fontSize = settings.translationSizeSp.sp,
                    lineHeight = (settings.translationSizeSp * 1.75f).sp,
                ),
                color = fg.copy(alpha = 0.8f),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            )
        }

        // Transliteration
        val translit = topic.transliteration["en_latin"]
        if (settings.showTransliteration && translit != null) {
            Text(
                text = translit,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BeVietnamPro),
                color = fg.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            )
        }

        // Full width divider
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            color = fg.copy(alpha = 0.2f),
            thickness = 1.2.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AyahActionSheet(
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onBookmark: () -> Unit,
    onMastered: () -> Unit,
    onShare: () -> Unit,
    isBookmarked: Boolean,
    isMastered: Boolean
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Ayah Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ActionRow(
                icon = Icons.Default.PlayArrow,
                label = "Play Audio",
                onClick = onPlay
            )
            ActionRow(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                onClick = onBookmark
            )
            ActionRow(
                icon = if (isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                label = if (isMastered) "Mark as Incomplete" else "Mark as Mastered",
                onClick = onMastered
            )
            ActionRow(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = onShare
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Word-by-word row ───────────────────────────────────────────────────────────

@Composable
private fun WordRow(
    words: List<WordData>,
    fg: Color,
    bg: Color,
    showTransliteration: Boolean,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(words, key = { it.id }) { word ->
            WordChip(word = word, fg = fg, bg = bg, showTransliteration = showTransliteration)
        }
    }
}

@Composable
private fun WordChip(
    word: WordData,
    fg: Color,
    bg: Color,
    showTransliteration: Boolean,
) {
    val chipBg = fg.copy(alpha = 0.06f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(chipBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text = word.arabic,
            fontSize = 20.sp,
            fontFamily = ScheherazadeNew,
            color = fg,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDirection = TextDirection.Rtl,
                fontFamily = ScheherazadeNew,
            ),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = word.translation,
            fontSize = 11.sp,
            fontFamily = BeVietnamPro,
            color = fg.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        if (showTransliteration && word.transliteration != null) {
            Text(
                text = word.transliteration,
                fontSize = 10.sp,
                fontFamily = BeVietnamPro,
                color = fg.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Reference badge ────────────────────────────────────────────────────────────

private fun getSurahName(number: String?): String? = when (number) {
    "1" -> "Al-Fatihah"
    "2" -> "Al-Baqarah"
    "49" -> "Al-Hujurat"
    "112" -> "Al-Ikhlas"
    else -> number?.let { "Surah $it" }
}

@Composable
private fun ReferenceBadge(
    source: String?,
    number: String?,
    fg: Color,
    modifier: Modifier = Modifier,
) {
    val formattedLabel = remember(source, number) {
        if (source?.lowercase() == "quran" && number != null) {
            val parts = number.split(":")
            val surahPart = parts.getOrNull(0)
            val ayahPart = parts.getOrNull(1)
            
            val surahName = getSurahName(surahPart)
            if (ayahPart != null) {
                "$surahName, Ayah $ayahPart"
            } else {
                surahName ?: source
            }
        } else {
            listOfNotNull(source, number).joinToString(" · ")
        }
    }

    if (formattedLabel.isBlank()) return

    Surface(
        shape = RoundedCornerShape(50),
        color = fg.copy(alpha = 0.08f),
        modifier = modifier,
    ) {
        Text(
            text = formattedLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = fg.copy(alpha = 0.5f),
            textAlign = TextAlign.End,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

// ── Settings bottom sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingSettingsSheet(
    topic: LearnTopic,
    settings: UserSettings,
    onDismiss: () -> Unit,
    onThemeChange: (String) -> Unit,
    onArabicSizeChange: (Float) -> Unit,
    onTranslationSizeChange: (Float) -> Unit,
    onShowTranslationChange: (Boolean) -> Unit,
    onShowTransliterationChange: (Boolean) -> Unit,
    onShowWordByWordChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onShowTajweedChange: (Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "Reading Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            // Theme picker
            SettingLabel("Display Theme")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("DARK" to "Dark", "PAPER" to "Paper", "LIGHT" to "Light").forEach { (value, label) ->
                    ThemeChip(
                        label = label,
                        selected = settings.readingTheme == value,
                        onClick = { onThemeChange(value) },
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Arabic size
            SettingLabel("Arabic Size  ${settings.arabicSizeSp.toInt()}sp")
            Slider(
                value = settings.arabicSizeSp,
                onValueChange = onArabicSizeChange,
                valueRange = 20f..60f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )

            // Translation size (only if translation is shown)
            if (settings.showTranslation) {
                SettingLabel("Translation Size  ${settings.translationSizeSp.toInt()}sp")
                Slider(
                    value = settings.translationSizeSp,
                    onValueChange = onTranslationSizeChange,
                    valueRange = 12f..24f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SettingToggle(
                label = "Show Translation",
                checked = settings.showTranslation,
                onCheckedChange = onShowTranslationChange,
            )
            SettingToggle(
                label = "Show Transliteration",
                checked = settings.showTransliteration,
                onCheckedChange = onShowTransliterationChange,
            )
            SettingToggle(
                label = "Show Word-by-Word",
                checked = settings.showWordByWord,
                onCheckedChange = onShowWordByWordChange,
            )
            SettingToggle(
                label = "Keep Screen On",
                checked = settings.readingKeepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
            )
            if (topic.tajweedMarkup != null) {
                SettingToggle(
                    label = "Tajweed Colors",
                    checked = settings.showTajweed,
                    onCheckedChange = onShowTajweedChange,
                )
            }
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val border = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BeVietnamPro),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    )
}

@Composable
private fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = BeVietnamPro),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}
