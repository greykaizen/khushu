package com.kaizen.khushu.ui.screens.quran

import android.widget.Toast
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaizen.khushu.data.model.AyahBlock
import com.kaizen.khushu.data.model.ContentBlock
import com.kaizen.khushu.data.model.ContentSource
import com.kaizen.khushu.data.model.AVAILABLE_RECITERS
import com.kaizen.khushu.ui.components.BlockActionSheet
import com.kaizen.khushu.ui.components.ReadingSettingsSheet
import com.kaizen.khushu.ui.components.TranslationPickerSheet
import com.kaizen.khushu.ui.components.TafsirPickerSheet
import com.kaizen.khushu.data.repository.TranslationRepository
import com.kaizen.khushu.data.repository.QuranAudioRepository
import com.kaizen.khushu.ui.screens.learn.BlockRenderer
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.ScheherazadeNew

// ── Theme helpers ──────────────────────────────────────────────────────────────

private val ThemeDark = Color.Black
private val ThemePaper = Color(0xFFF5E6C8)
private val ThemeLight = Color.White

@Composable
private fun bgColor(theme: String) : Color {
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    return when (theme) {
        "DARK" -> ThemeDark
        "PAPER" -> ThemePaper
        "LIGHT" -> ThemeLight
        else -> if (isSystemDark) ThemeDark else ThemeLight
    }
}

@Composable
private fun contentColor(theme: String) : Color {
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    return when (theme) {
        "DARK" -> Color.White
        "PAPER", "LIGHT" -> Color.Black
        else -> if (isSystemDark) Color.White else Color.Black
    }
}

@Composable
private fun readingColorScheme(readingTheme: String, dynamicColor: Boolean): ColorScheme {
    val context = LocalContext.current
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when(readingTheme) {
        "DARK" -> true
        "LIGHT" -> false
        "PAPER" -> false
        else -> isSystemDark
    }
    
    return when {
        isDark && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(context)
        isDark -> darkColorScheme()
        !isDark && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        else -> lightColorScheme()
    }
}

@Composable
private fun JuzHeader(juzNumber: Int, contentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = contentColor.copy(alpha = 0.15f),
        )
        Text(
            text = "Juz $juzNumber",
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 12.dp),
            fontFamily = BeVietnamPro,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = contentColor.copy(alpha = 0.15f),
        )
    }
}

@Composable
private fun SajdaIndicator(type: String, contentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    if (type == "obligatory") MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    androidx.compose.foundation.shape.CircleShape
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (type == "obligatory") "Sajdah Wajibah" else "Sajdah Mustahabb",
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.5f),
            fontFamily = BeVietnamPro,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    onBack: () -> Unit,
    onNextSurah: (Int) -> Unit,
    viewModel: QuranViewModel,
    settingsViewModel: SettingsViewModel,
    media3Controller: MediaController?,
    quranAudioViewModel: QuranAudioViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val ayahs by viewModel.currentAyahs
    val translations by viewModel.currentTranslation
    val scriptMap by viewModel.scriptMap
    val isLoading by viewModel.isLoading
    val tafsirText by viewModel.tafsirText
    val isTafsirDownloading by viewModel.isTafsirDownloading
    val tafsirDownloadProgress by viewModel.tafsirDownloadProgress
    val chapters by viewModel.chapters
    val settings by settingsViewModel.settings.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val listState = rememberLazyListState()

    val playingAyahIndex by quranAudioViewModel.playingAyahIndex
    val audioState by quranAudioViewModel.audioState

    // --- Overscroll / Next Surah Logic ---
    val thresholdPx = with(density) { 240.dp.toPx() }
    val overscrollState = remember { mutableFloatStateOf(0f) }
    val fillProgress = (overscrollState.floatValue / thresholdPx).coerceIn(0f, 1f)

    val nextSurah = remember(surahNumber, chapters) {
        if (surahNumber < 114) chapters.find { it.id == surahNumber + 1 } else null
    }

    val nestedScrollConnection = remember(listState, nextSurah) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (nextSurah == null) return androidx.compose.ui.geometry.Offset.Zero
                if (source != androidx.compose.ui.input.nestedscroll.NestedScrollSource.UserInput) return androidx.compose.ui.geometry.Offset.Zero

                // Only detect upward drag when at the very bottom
                if (available.y < 0f && !listState.canScrollForward) {
                    val prev = overscrollState.floatValue
                    overscrollState.floatValue = (prev - available.y).coerceAtMost(thresholdPx)

                    if (overscrollState.floatValue >= thresholdPx && prev < thresholdPx) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }
                    return androidx.compose.ui.geometry.Offset(0f, available.y)
                }
                
                // INSTANT ABORT: If user swipes down while bar is showing, reset to 0
                if (available.y > 0f && overscrollState.floatValue > 0f) {
                    overscrollState.floatValue = 0f
                    return androidx.compose.ui.geometry.Offset(0f, available.y)
                }

                return androidx.compose.ui.geometry.Offset.Zero
            }

            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (source == androidx.compose.ui.input.nestedscroll.NestedScrollSource.UserInput && available.y > 0f && !listState.canScrollForward) {
                    // Safety reset
                    overscrollState.floatValue = 0f
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: androidx.compose.ui.unit.Velocity,
                available: androidx.compose.ui.unit.Velocity
            ): androidx.compose.ui.unit.Velocity {
                if (overscrollState.floatValue >= thresholdPx) {
                    onNextSurah(surahNumber + 1)
                }
                overscrollState.floatValue = 0f
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }
    // --------------------------------------

    LaunchedEffect(media3Controller) {
        quranAudioViewModel.setController(media3Controller)
    }

    val scheme = readingColorScheme(settings.readingTheme, settings.dynamicColor)

    var showSettings by remember { mutableStateOf(false) }
    var showTranslationPicker by remember { mutableStateOf(false) }
    var showTafsirPicker by remember { mutableStateOf(false) }

    LaunchedEffect(surahNumber, settings.selectedTranslationLang) {
        viewModel.loadChapters()
        viewModel.loadSurah(surahNumber, settings.selectedTranslationLang)
        viewModel.loadVerseMeta(context)
    }

    LaunchedEffect(surahNumber, settings.showTafsir, settings.selectedTafsirId) {
        viewModel.loadTafsirIfEnabled(context, settings, surahNumber)
    }

    // Load script when selected script changes
    LaunchedEffect(settings.selectedScript) {
        viewModel.loadScript(context, settings.selectedScript)
    }

    val surah = chapters.find { it.id == surahNumber }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Auto-scroll to playing ayah
    LaunchedEffect(playingAyahIndex) {
        playingAyahIndex?.let { index ->
            val offset = if (surahNumber != 1 && surahNumber != 9) 1 else 0
            listState.animateScrollToItem(index + offset)
        }
    }

    // Handle audio errors
    LaunchedEffect(audioState) {
        if (audioState is QuranAudioViewModel.AudioState.Error) {
            Toast.makeText(context, (audioState as QuranAudioViewModel.AudioState.Error).msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Convert ayahs to blocks for BlockRenderer
    val blocks = remember(ayahs, translations, tafsirText, surah) {
        ayahs.map { (num, text) ->
            val plainText = text.replace(Regex("<[^>]*>"), "")

            AyahBlock(
                surah = surahNumber,
                ayah = num,
                display = "Surah ${surah?.nameSimple ?: surahNumber}, Ayah $num",
                textUthmani = plainText,
                tajweedMarkup = if (text.contains("<tajweed")) text else null,
                translationEn = translations[num],
                tafsirText = tafsirText[num],
                verified = true
            )
        }
    }

    MaterialTheme(colorScheme = scheme) {
        val bg = bgColor(settings.readingTheme)
        val fg = contentColor(settings.readingTheme)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(32.dp))
                .background(bg)
        ) {
            val titleFraction = scrollBehavior.state.collapsedFraction
            val titleFontSize = androidx.compose.ui.util.lerp(28f, 20f, titleFraction).sp

            Scaffold(
                containerColor = Color.Transparent,
                modifier = modifier
                    .nestedScroll(nestedScrollConnection)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeTopAppBar(
                        title = {
                            surah?.let {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = it.nameSimple,
                                        fontFamily = BeVietnamPro,
                                        fontSize = titleFontSize,
                                        color = fg
                                    )
                                    Text(
                                        text = "·",
                                        fontFamily = BeVietnamPro,
                                        fontSize = titleFontSize,
                                        color = fg.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = it.nameArabic,
                                        fontFamily = ScheherazadeNew,
                                        fontSize = titleFontSize * 1.1f,
                                        color = fg
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = fg
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                if (audioState == QuranAudioViewModel.AudioState.Playing) {
                                    quranAudioViewModel.pause()
                                } else if (audioState == QuranAudioViewModel.AudioState.Paused) {
                                    quranAudioViewModel.resume()
                                } else {
                                    val url = QuranAudioRepository.getUrl(context, settings.selectedReciterId, surahNumber)
                                    if (url != null) {
                                        quranAudioViewModel.playSurah(surahNumber, url, settings.selectedReciterId)
                                    }
                                }
                            }) {
                                when (audioState) {
                                    is QuranAudioViewModel.AudioState.Playing -> Icon(Icons.Default.Pause, "Pause", tint = fg.copy(alpha = 0.7f))
                                    is QuranAudioViewModel.AudioState.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = fg.copy(alpha = 0.7f))
                                    else -> Icon(Icons.Default.PlayArrow, "Play", tint = fg.copy(alpha = 0.7f))
                                }
                            }

                            IconButton(onClick = { showSettings = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = fg.copy(alpha = 0.7f)
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = bg.copy(alpha = 0.9f),
                            titleContentColor = fg
                        )
                    )
                }
            ) { paddingValues ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                    color = if (settings.readingTheme == "DARK") Color.Black else if (settings.readingTheme == "PAPER") Color(0xFFFBF4E9) else MaterialTheme.colorScheme.surface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        var activeBlock by remember { mutableStateOf<Pair<ContentBlock, Int>?>(null) }

                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = 32.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (surahNumber != 9 && surahNumber != 1) {
                                item {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        textAlign = TextAlign.Center,
                                        fontFamily = ScheherazadeNew,
                                        fontSize = 28.sp,
                                        color = fg,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp)
                                    )
                                }
                            }

                            itemsIndexed(blocks) { index, block ->
                                val ayahNum = block.ayah
                                val meta = viewModel.verseMeta.value["$surahNumber:$ayahNum"]
                                val prevMeta = if (ayahNum > 1) viewModel.verseMeta.value["$surahNumber:${ayahNum - 1}"] else null
                                val showJuzHeader = meta != null && (prevMeta == null || prevMeta.juz != meta.juz)

                                Column {
                                    if (showJuzHeader && ayahNum > 1) {
                                        JuzHeader(juzNumber = meta!!.juz, contentColor = fg)
                                    }
                                    if (meta?.sajda != null) {
                                        SajdaIndicator(type = meta.sajda, contentColor = fg)
                                    }

                                    val translationMap = remember(translations) {
                                        translations.mapKeys { it.key.toString() }
                                    }
                                    BlockRenderer(
                                        block = block,
                                        settings = settings,
                                        fg = fg,
                                        bg = bg,
                                        translationMap = translationMap,
                                        scriptMap = scriptMap,
                                        isHighlighted = playingAyahIndex == index,
                                        onBlockClick = { activeBlock = it to index },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }

                            item {
                                val isArmed = fillProgress >= 1f
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 64.dp, top = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "--- End of Surah ---",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = fg.copy(alpha = 0.15f)
                                    )

                                    if (nextSurah != null) {
                                        Spacer(Modifier.height(24.dp))

                                        // Circle hidden until swiping
                                        if (overscrollState.floatValue > 0f) {
                                            Box(
                                                modifier = Modifier.size(36.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    progress = { fillProgress },
                                                    modifier = Modifier.fillMaxSize(),
                                                    color = if (isArmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                    strokeWidth = 3.dp,
                                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                                )
                                                if (isArmed) {
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowUp,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(12.dp))
                                        }

                                        Text(
                                            text = if (isArmed) "Release for Next Surah" else "Swipe up for Next Surah",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isArmed) MaterialTheme.colorScheme.primary else fg.copy(alpha = 0.3f)
                                        )

                                        // Only show next surah name after threshold is met
                                        if (isArmed) {
                                            Text(
                                                text = nextSurah.nameSimple,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (activeBlock != null) {
                            val (block, index) = activeBlock!!
                            val topicId = "quran_surah_$surahNumber"
                            val isBookmarked = settings.bookmarkedAyahs.contains("$topicId:$index")

                            BlockActionSheet(
                                block = block,
                                topicId = topicId,
                                settings = settings,
                                isBookmarked = isBookmarked,
                                onDismiss = { activeBlock = null },
                                onBookmark = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    settingsViewModel.toggleBookmark(topicId, index)
                                    val msg = if (isBookmarked) "Bookmark removed" else "Bookmark added"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    activeBlock = null
                                },
                                onPlayAyah = {
                                    quranAudioViewModel.playAyah(surahNumber, index, blocks, settings.selectedReciterId, sequence = false)
                                    activeBlock = null
                                },
                                onPlayFromHere = {
                                    quranAudioViewModel.playAyah(surahNumber, index, blocks, settings.selectedReciterId, sequence = true)
                                    activeBlock = null
                                }
                            )
                        }
                    }
                }
            }

            if (showSettings) {
                val reciterDownloadStates = AVAILABLE_RECITERS.associate { reciter ->
                    reciter.id to quranAudioViewModel.getReciterDownloadProgress(reciter.id).collectAsState(initial = null).value
                }

                ReadingSettingsSheet(
                    settings = settings,
                    supportsTafsirSelection = true,
                    reciterDownloadStates = reciterDownloadStates,
                    isReciterDownloaded = { quranAudioViewModel.isReciterDownloaded(it) },
                    onDismiss = { showSettings = false },
                    onThemeChange = { settingsViewModel.setReadingTheme(it) },
                    onArabicSizeChange = { settingsViewModel.setArabicSizeSp(it) },
                    onTranslationSizeChange = { settingsViewModel.setTranslationSizeSp(it) },
                    onShowTranslationChange = { settingsViewModel.toggleShowTranslation(it) },
                    onShowTransliterationChange = { settingsViewModel.toggleShowTransliteration(it) },
                    onShowWordByWordChange = { settingsViewModel.toggleShowWordByWord(it) },
                    onKeepScreenOnChange = { settingsViewModel.toggleReadingKeepScreenOn(it) },
                    onShowTajweedChange = { settingsViewModel.toggleShowTajweed(it) },
                    onTranslationLangChange = { settingsViewModel.setSelectedTranslationLang(it) },
                    onShowTafsirChange = { settingsViewModel.setShowTafsir(it) },
                    onOpenTafsirPicker = {
                        showSettings = false
                        showTafsirPicker = true
                    },
                    onReciterChange = { settingsViewModel.setSelectedReciterId(it) },
                    onScriptChange = { settingsViewModel.setSelectedScript(it) },
                    onOpenTranslationPicker = {
                        showSettings = false
                        showTranslationPicker = true
                    },
                    onDownloadAudio = { reciterId ->
                        quranAudioViewModel.downloadReciter(reciterId)
                    },
                    onAudioSourceChange = { settingsViewModel.setSelectedAudioSource(it) }
                )
            }

            if (showTranslationPicker) {
                val translationViewModel: com.kaizen.khushu.ui.screens.learn.LearnReadingViewModel = viewModel()
                TranslationPickerSheet(
                    selectedId = settings.selectedTranslationLang,
                    selectedSource = try { ContentSource.valueOf(settings.selectedTranslationSource) } catch (e: Exception) { ContentSource.FAWAZ },
                    isDownloading = translationViewModel.isDownloading.value,
                    progress = translationViewModel.downloadProgress.floatValue,
                    onSelectSource = { source ->
                        settingsViewModel.setSelectedTranslationSource(source.name)
                    },
                    onSelect = { meta ->
                        if (TranslationRepository.isDownloaded(context, meta.id)) {
                            settingsViewModel.setSelectedTranslationLang(meta.id)
                            showTranslationPicker = false
                        } else {
                            translationViewModel.downloadTranslation(context, meta) {
                                settingsViewModel.setSelectedTranslationLang(meta.id)
                                showTranslationPicker = false
                            }
                        }
                    },
                    onDismiss = { showTranslationPicker = false }
                )
            }

            if (showTafsirPicker) {
                TafsirPickerSheet(
                    selectedTafsirId = settings.selectedTafsirId,
                    selectedSource = try { ContentSource.valueOf(settings.selectedTafsirSource) } catch (e: Exception) { ContentSource.SPA5K },
                    currentSurah = surahNumber,
                    isDownloading = isTafsirDownloading,
                    progress = tafsirDownloadProgress,
                    downloadingTafsirId = viewModel.downloadingTafsirId.value,
                    onSelectSource = { source ->
                        settingsViewModel.setSelectedTafsir(settings.selectedTafsirId, source.name)
                    },
                    onSelect = { meta ->
                        if (com.kaizen.khushu.data.repository.TafsirRepository.isDownloaded(context, meta.id, surahNumber)) {
                            settingsViewModel.setSelectedTafsir(meta.id, meta.source.name)
                            showTafsirPicker = false
                        } else {
                            viewModel.downloadTafsirForSurah(context, meta, surahNumber) {
                                settingsViewModel.setSelectedTafsir(meta.id, meta.source.name)
                                showTafsirPicker = false
                            }
                        }
                    },
                    onDismiss = { showTafsirPicker = false }
                )
            }
        }
    }
}
