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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaizen.khushu.data.model.AyahBlock
import com.kaizen.khushu.data.model.ContentBlock
import com.kaizen.khushu.data.model.AVAILABLE_RECITERS
import com.kaizen.khushu.ui.components.BlockActionSheet
import com.kaizen.khushu.ui.components.ReadingSettingsSheet
import com.kaizen.khushu.ui.components.TranslationPickerSheet
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    onBack: () -> Unit,
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
    val chapters by viewModel.chapters
    val settings by settingsViewModel.settings.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    
    val playingAyahIndex by quranAudioViewModel.playingAyahIndex
    val audioState by quranAudioViewModel.audioState
    
    LaunchedEffect(media3Controller) {
        quranAudioViewModel.setController(media3Controller)
    }
    
    val scheme = readingColorScheme(settings.readingTheme, settings.dynamicColor)
    
    var showSettings by remember { mutableStateOf(false) }
    var showTranslationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(surahNumber, settings.selectedTranslationLang) {
        viewModel.loadChapters()
        viewModel.loadSurah(surahNumber, settings.selectedTranslationLang)
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
    val blocks = remember(ayahs, translations, surah) {
        ayahs.map { (num, text) ->
            val plainText = text.replace(Regex("<[^>]*>"), "")
            
            AyahBlock(
                surah = surahNumber,
                ayah = num,
                display = "Surah ${surah?.nameSimple ?: surahNumber}, Ayah $num",
                textUthmani = plainText,
                tajweedMarkup = if (text.contains("<tajweed")) text else null,
                translationEn = translations[num],
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
                modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                            contentPadding = PaddingValues(bottom = 100.dp),
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
                    translationLanguages = com.kaizen.khushu.data.model.AVAILABLE_TRANSLATIONS.map { it.id }.toSet(),
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
                    onReciterChange = { settingsViewModel.setSelectedReciterId(it) },
                    onScriptChange = { settingsViewModel.setSelectedScript(it) },
                    onOpenTranslationPicker = {
                        showSettings = false
                        showTranslationPicker = true
                    },
                    onDownloadAudio = { reciterId ->
                        quranAudioViewModel.downloadReciter(reciterId)
                    }
                )
            }

            if (showTranslationPicker) {
                val translationViewModel: com.kaizen.khushu.ui.screens.learn.LearnReadingViewModel = viewModel()
                TranslationPickerSheet(
                    selectedId = settings.selectedTranslationLang,
                    isDownloading = translationViewModel.isDownloading.value,
                    progress = translationViewModel.downloadProgress.floatValue,
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
        }
    }
}
