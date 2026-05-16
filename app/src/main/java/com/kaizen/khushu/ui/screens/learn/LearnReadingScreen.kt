package com.kaizen.khushu.ui.screens.learn

import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import com.kaizen.khushu.data.model.*
import com.kaizen.khushu.data.repository.TranslationRepository
import com.kaizen.khushu.ui.components.BlockActionSheet
import com.kaizen.khushu.ui.components.ReadingSettingsSheet
import com.kaizen.khushu.ui.components.TranslationPickerSheet
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.ScheherazadeNew
import com.kaizen.khushu.ui.theme.rememberArabicScriptFontFamily

// ── Translation helpers ────────────────────────────────────────────────────────

private val RtlLangs = setOf("ur", "ar", "fa", "he")
private fun translationDirection(lang: String) =
    if (lang in RtlLangs) TextDirection.Rtl else TextDirection.Ltr

// ── Theme helpers ──────────────────────────────────────────────────────────────

private val ThemeDark = Color.Black
private val ThemePaper = Color(0xFFF5E6C8)
private val ThemeLight = Color.White

@Composable
private fun bgColor(theme: String) : Color {
    val isSystemDark = isSystemInDarkTheme()
    return when (theme) {
        "DARK" -> ThemeDark
        "PAPER" -> ThemePaper
        "LIGHT" -> ThemeLight
        else -> if (isSystemDark) ThemeDark else ThemeLight
    }
}

@Composable
private fun contentColor(theme: String) : Color {
    val isSystemDark = isSystemInDarkTheme()
    return when (theme) {
        "DARK" -> Color.White
        "PAPER", "LIGHT" -> Color.Black
        else -> if (isSystemDark) Color.White else Color.Black
    }
}

@Composable
private fun readingColorScheme(readingTheme: String, dynamicColor: Boolean): ColorScheme {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
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

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnReadingScreen(
    topic: LearnTopic,
    settingsViewModel: SettingsViewModel,
    learnAudioViewModel: LearnAudioViewModel,
    media3Controller: MediaController?,
    learnReadingViewModel: LearnReadingViewModel = viewModel(),
    onBack: () -> Unit,
    initialAyahIndex: Int? = null,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(media3Controller) {
        learnAudioViewModel.setController(media3Controller)
    }

    val settings by settingsViewModel.settings.collectAsState()
    val availableQuranScripts by settingsViewModel.availableQuranScripts.collectAsState()
    val downloadingQuranScript by settingsViewModel.downloadingQuranScript.collectAsState()
    val quranScriptDownloadProgress by settingsViewModel.quranScriptDownloadProgress.collectAsState()
    val arabicScriptFontFamily = rememberArabicScriptFontFamily(settings.selectedScript, availableQuranScripts)
    val scheme = readingColorScheme(settings.readingTheme, settings.dynamicColor)

    MaterialTheme(colorScheme = scheme) {
        val audioState by learnAudioViewModel.audioState.collectAsState()
        val blocks by learnReadingViewModel.blocks.collectAsState()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val listState = rememberLazyListState()
        var showSettings by remember { mutableStateOf(false) }
        var showTranslationPicker by remember { mutableStateOf(false) }
        
        val translationMap by learnReadingViewModel.translationMap
        val tajweedMap by learnReadingViewModel.tajweedMap
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current

        val bg = bgColor(settings.readingTheme)
        val fg = contentColor(settings.readingTheme)

        val titleFraction = scrollBehavior.state.collapsedFraction
        val titleFontSize = androidx.compose.ui.util.lerp(28f, 20f, titleFraction).sp

        LaunchedEffect(settings.selectedTranslationLang) {
            learnReadingViewModel.loadTranslation(context, settings.selectedTranslationLang)
        }

        LaunchedEffect(topic.id) {
            learnReadingViewModel.loadBlocks(topic.id)
            settingsViewModel.updateLastReadTopicId(topic.id)
        }

        LaunchedEffect(initialAyahIndex, blocks) {
            if (initialAyahIndex != null && blocks != null) {
                val offset = if (blocks!!.isNotEmpty()) 1 else 0
                listState.animateScrollToItem(initialAyahIndex + offset)
            }
        }

        val activity = LocalActivity.current
        DisposableEffect(settings.readingKeepScreenOn) {
            if (settings.readingKeepScreenOn) {
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            onDispose {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        val isDarkReading = settings.readingTheme == "DARK" || (settings.readingTheme == "SYSTEM" && isSystemInDarkTheme())
        DisposableEffect(isDarkReading) {
            val window = activity?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDarkReading
            }
            onDispose {}
        }

        Box(modifier = Modifier.fillMaxSize()
            .clip(RoundedCornerShape(32.dp))
            .background(bg)) {
            Scaffold(
                containerColor = Color.Transparent,
                modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeTopAppBar(
                        title = {
                            Text(text = topic.title, fontFamily = BeVietnamPro, fontSize = titleFontSize, fontWeight = FontWeight.Normal)
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = fg) }
                        },
                        actions = {
                            if (topic.audioFilename != null) {
                                IconButton(onClick = {
                                    if (audioState is LearnAudioViewModel.AudioState.Playing) learnAudioViewModel.pause()
                                    else learnAudioViewModel.play(topic)
                                }) {
                                    if (audioState is LearnAudioViewModel.AudioState.Playing) Icon(Icons.Default.Pause, "Pause", tint = fg.copy(alpha = 0.7f))
                                    else Icon(Icons.Default.PlayArrow, "Play", tint = fg.copy(alpha = 0.7f))
                                }
                            }
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, "Settings", tint = fg.copy(alpha = 0.7f))
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = bg.copy(alpha = 0.9f),
                            titleContentColor = fg,
                            navigationIconContentColor = fg
                        )
                    )
                }
            ) { paddingValues ->
                Surface(
                    modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()),
                    color = if (isDarkReading) Color.Black else if (settings.readingTheme == "PAPER") Color(0xFFFBF4E9) else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    var activeBlock by remember { mutableStateOf<Pair<ContentBlock, Int>?>(null) }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        blocks?.let { resolvedBlocks ->
                            itemsIndexed(resolvedBlocks) { index, block ->
                                BlockRenderer(
                                    block = block,
                                    settings = settings,
                                    fg = fg,
                                    bg = bg,
                                    translationMap = translationMap,
                                    tajweedMap = tajweedMap,
                                    arabicFontFamily = arabicScriptFontFamily,
                                    onBlockClick = { activeBlock = it to index },
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (activeBlock != null) {
                        val (block, index) = activeBlock!!
                        BlockActionSheet(
                            block = block,
                            topicId = topic.id,
                            settings = settings,
                            isBookmarked = settings.bookmarkedAyahs.contains("${topic.id}:$index"),
                            onDismiss = { activeBlock = null },
                            onBookmark = {
                                settingsViewModel.toggleBookmark(topic.id, index)
                                activeBlock = null
                            }
                        )
                    }
                }
            }

            if (showSettings) {
                ReadingSettingsSheet(
                    settings = settings,
                    isQuranContext = false,
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
                    onOpenTafsirPicker = { showSettings = false },
                    onReciterChange = { settingsViewModel.setSelectedReciterId(it) },
                    onScriptChange = { settingsViewModel.setSelectedScript(it) },
                    availableQuranScripts = availableQuranScripts,
                    downloadingQuranScript = downloadingQuranScript,
                    quranScriptDownloadProgress = quranScriptDownloadProgress,
                    onDownloadQuranScript = { settingsViewModel.downloadQuranScript(it) },
                    onOpenTranslationPicker = { showSettings = false; showTranslationPicker = true },
                    onDownloadAudio = {}
                )
            }

            if (showTranslationPicker) {
                TranslationPickerSheet(
                    selectedId = settings.selectedTranslationLang,
                    selectedSource = try { com.kaizen.khushu.data.model.ContentSource.valueOf(settings.selectedTranslationSource) } catch (e: Exception) { com.kaizen.khushu.data.model.ContentSource.FAWAZ },
                    isDownloading = learnReadingViewModel.isDownloading.value,
                    progress = learnReadingViewModel.downloadProgress.floatValue,
                    onSelectSource = { source ->
                        settingsViewModel.setSelectedTranslationSource(source.name)
                    },
                    onSelect = { meta -> settingsViewModel.setSelectedTranslationLang(meta.id); showTranslationPicker = false },
                    onDismiss = { showTranslationPicker = false }
                )
            }
        }
    }
}
