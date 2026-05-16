package com.kaizen.khushu.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.model.ContentSource
import com.kaizen.khushu.data.repository.CatalogRepository
import com.kaizen.khushu.data.model.TranslationMeta
import com.kaizen.khushu.data.model.AVAILABLE_RECITERS
import com.kaizen.khushu.data.repository.TranslationRepository
import com.kaizen.khushu.data.repository.QuranScriptFontRepository
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.ui.theme.Antonio
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.ScheherazadeNew

@Composable
private fun SlidingSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp)
    ) {
        val itemWidth = maxWidth / items.size
        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = tween(durationMillis = 220),
            label = "reading_settings_segment_offset"
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
            ) {}

            Row(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(index) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = BeVietnamPro,
                            color = if (selectedIndex == index) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSettingsSheet(
    settings: UserSettings,
    reciterDownloadStates: Map<String, Pair<Float, Int>?> = emptyMap(),
    isReciterDownloaded: (String) -> Boolean = { false },
    supportsTafsirSelection: Boolean = false,
    isQuranContext: Boolean = true,
    onDismiss: () -> Unit,
    onThemeChange: (String) -> Unit,
    onArabicSizeChange: (Float) -> Unit,
    onTranslationSizeChange: (Float) -> Unit,
    onShowTranslationChange: (Boolean) -> Unit,
    onShowTransliterationChange: (Boolean) -> Unit,
    onShowWordByWordChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onShowTajweedChange: (Boolean) -> Unit,
    onTranslationLangChange: (String) -> Unit,
    onShowTafsirChange: (Boolean) -> Unit,
    onOpenTafsirPicker: () -> Unit,
    onReciterChange: (String) -> Unit,
    onScriptChange: (String) -> Unit,
    availableQuranScripts: Set<String> = QuranScriptFontRepository.bundledScripts(),
    downloadingQuranScript: String? = null,
    quranScriptDownloadProgress: Float = 0f,
    onDownloadQuranScript: (String) -> Unit = {},
    onOpenTranslationPicker: () -> Unit,
    onDownloadAudio: (String) -> Unit,
    onAudioSourceChange: (String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = if (isQuranContext) {
        listOf("Display", "Text", "Audio")
    } else {
        listOf("Display")
    }

    LaunchedEffect(isQuranContext) {
        if (!isQuranContext) selectedTab = 0
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Reading Settings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.padding(horizontal = 28.dp)
                        .padding(bottom = 16.dp)
                )

                // LIVE PREVIEW BOX
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                            Text(
                                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                fontFamily = ScheherazadeNew,
                                fontSize = settings.arabicSizeSp.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (settings.showTranslation) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                                fontFamily = BeVietnamPro,
                                fontSize = settings.translationSizeSp.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                SlidingSegmentedControl(
                    items = tabs,
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Using LazyColumn for the whole tab content to ensure full scrollability
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // Display
                            item {
                                SettingLabel("Background Theme")
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val systemIsDark = isSystemInDarkTheme()
                                    listOf(
                                        Triple("SYSTEM", "Auto", (if (systemIsDark) Color.Black else Color.White) to (if (systemIsDark) Color.White else Color.Black)),
                                        Triple("DARK", "Dark", Color.Black to Color.White),
                                        Triple("PAPER", "Paper", Color(0xFFF5E6C8) to Color.Black),
                                        Triple("LIGHT", "Light", Color.White to Color.Black)
                                    ).forEach { (value, label, colors) ->
                                        ThemePreviewCard(
                                            label = label,
                                            selected = settings.readingTheme == value,
                                            bgColor = colors.first,
                                            fgColor = colors.second,
                                            onClick = { onThemeChange(value) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            item {
                                SettingToggle(label = "Keep Screen On", checked = settings.readingKeepScreenOn, onCheckedChange = onKeepScreenOnChange)
                            }
                        }
                        1 -> { // Text & Translation
                            if (!isQuranContext) {
                                item { Spacer(Modifier.height(1.dp)) }
                            } else {
                            item {
                                val context = LocalContext.current
                                val currentMeta = remember(settings.selectedTranslationLang, settings.selectedTranslationSource) {
                                    val source = try { com.kaizen.khushu.data.model.ContentSource.valueOf(settings.selectedTranslationSource) } catch (_: Exception) { com.kaizen.khushu.data.model.ContentSource.FAWAZ }
                                    CatalogRepository.translations(context, source).find { it.id == settings.selectedTranslationLang }
                                        ?: com.kaizen.khushu.data.model.TranslationMeta.bundledEnglish().takeIf { it.id == settings.selectedTranslationLang }
                                        ?: com.kaizen.khushu.data.model.TranslationMeta.bundledUrdu().takeIf { it.id == settings.selectedTranslationLang }
                                }
                                val langLabel = currentMeta?.let { "${it.langCode.uppercase()} • ${it.translatorName}" } ?: "Select Translation"

                                SettingLabel("Active Translation")
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = onOpenTranslationPicker,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(Icons.Default.Language, null, modifier = Modifier.size(20.dp))
                                        Text(langLabel, style = MaterialTheme.typography.bodyLarge, fontFamily = BeVietnamPro, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            if (supportsTafsirSelection) {
                                item {
                                    val context = LocalContext.current
                                    val tafsirSource = remember(settings.selectedTafsirSource) {
                                        try { ContentSource.valueOf(settings.selectedTafsirSource) } catch (_: Exception) { ContentSource.SPA5K }
                                    }
                                    val effectiveTafsirSource = remember(tafsirSource) {
                                        if (tafsirSource == ContentSource.QF) ContentSource.SPA5K else tafsirSource
                                    }
                                    val selectedTafsirMeta = remember(
                                        settings.selectedTafsirId,
                                        effectiveTafsirSource
                                    ) {
                                        CatalogRepository.tafsirs(context, effectiveTafsirSource)
                                            .find { it.id == settings.selectedTafsirId }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                    Spacer(Modifier.height(8.dp))
                                    SettingLabel("Tafsir")
                                    Spacer(Modifier.height(12.dp))
                                    SettingToggle(
                                        label = "Show Tafsir",
                                        checked = settings.showTafsir,
                                        onCheckedChange = onShowTafsirChange,
                                    )
                                    if (settings.showTafsir) {
                                        Spacer(Modifier.height(12.dp))
                                        val tafsirLabel = selectedTafsirMeta?.name
                                            ?: settings.selectedTafsirId.ifBlank { "Select Tafsir" }
                                        OutlinedButton(
                                            onClick = onOpenTafsirPicker,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                        ) {
                                            Text(tafsirLabel, fontFamily = BeVietnamPro)
                                        }
                                    }
                                }
                            }

                            item {
                                SettingLabel("Script Style")
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        Triple("uthmani", "Uthmani", "ٱلصَّلَوٰةِ"),
                                        Triple("indopak", "IndoPak", "الصلوة"),
                                        Triple("uthmani_simple", "Simple", "الصلوة"),
                                        Triple("imlaei", "Imlaei", "الصلاة"),
                                        Triple(QuranScriptFontRepository.UTHMANIC_HAFS, "Uthmanic Hafs", "ٱلصَّلَوٰةِ")
                                    ).forEach { (value, label, sample) ->
                                        val isDownloaded = availableQuranScripts.contains(value)
                                        val isDownloading = downloadingQuranScript == value
                                        ScriptPreviewCard(
                                            label = label,
                                            sample = sample,
                                            selected = settings.selectedScript == value,
                                            isDownloaded = isDownloaded,
                                            isDownloading = isDownloading,
                                            downloadProgress = if (isDownloading) quranScriptDownloadProgress else 0f,
                                            onClick = {
                                                if (isDownloaded) onScriptChange(value)
                                                else onDownloadQuranScript(value)
                                            },
                                            onDownloadClick = { onDownloadQuranScript(value) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            item {
                                SettingLabel("Arabic Text Size: ${settings.arabicSizeSp.toInt()}sp")
                                Slider(
                                    value = settings.arabicSizeSp,
                                    onValueChange = onArabicSizeChange,
                                    valueRange = 16f..64f,
                                    steps = 11 // 16,20,24,28,32,36,40,44,48,52,56,60,64
                                )
                            }

                            if (settings.showTranslation) {
                                item {
                                    SettingLabel("Translation Size: ${settings.translationSizeSp.toInt()}sp")
                                    Slider(
                                        value = settings.translationSizeSp,
                                        onValueChange = onTranslationSizeChange,
                                        valueRange = 12f..28f,
                                        steps = 7
                                    )
                                }
                            }

                            item {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    SettingToggle(label = "Tajweed Colors", checked = settings.showTajweed, onCheckedChange = onShowTajweedChange)
                                    SettingToggle(label = "Show Translation", checked = settings.showTranslation, onCheckedChange = onShowTranslationChange)
                                    SettingToggle(label = "Show Transliteration", checked = settings.showTransliteration, onCheckedChange = onShowTransliterationChange)
                                    SettingToggle(label = "Show Word-by-Word", checked = settings.showWordByWord, onCheckedChange = onShowWordByWordChange)
                                }
                            }
                            }
                        }
                        2 -> { // Audio
                            if (isQuranContext) item {
                                val audioSources = remember { ContentSource.entries.filter { it.supportsAudio } }
                                val selectedAudioSource = remember(settings.selectedAudioSource) { try { ContentSource.valueOf(settings.selectedAudioSource) } catch (_: Exception) { ContentSource.MP3QURAN } }
                                val context = LocalContext.current
                                val reciters = remember(selectedAudioSource) { CatalogRepository.reciters(context, selectedAudioSource) }

                                Column {
                                    SettingLabel("Source")
                                    Spacer(Modifier.height(8.dp))
                                    SourcePickerRow(
                                        sources = audioSources,
                                        selected = selectedAudioSource,
                                        onSelect = { onAudioSourceChange(it.name) }
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    SettingLabel("Select Reciter")
                                    Spacer(Modifier.height(8.dp))
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    reciters.forEach { reciter ->
                                        val isSelected = settings.selectedReciterId == reciter.id
                                        val isDownloaded = isReciterDownloaded(reciter.id)
                                        val downloadState = reciterDownloadStates[reciter.id]

                                        Surface(
                                            onClick = { onReciterChange(reciter.id) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                                            contentDescription = null,
                                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )
                                                        
                                                        Column {
                                                            Text(
                                                                text = reciter.name,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontFamily = BeVietnamPro,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                            Text(
                                                                text = reciter.style,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontFamily = BeVietnamPro,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                            )
                                                        }
                                                    }

                                                    if (isDownloaded) {
                                                        Icon(Icons.Default.Check, "Downloaded", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                                    } else if (downloadState == null) {
                                                        IconButton(
                                                            onClick = { onDownloadAudio(reciter.id) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.Download, "Download", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                        }
                                                    }
                                                }

                                                // Progress Bar
                                                if (downloadState != null && downloadState.first < 1f) {
                                                    val progress = downloadState.first
                                                    val count = downloadState.second
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        LinearProgressIndicator(
                                                            progress = { progress },
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                        Text(
                                                            text = "$count/114",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontFamily = BeVietnamPro,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontFamily = BeVietnamPro,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontFamily = BeVietnamPro, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun ThemePreviewCard(
    label: String,
    selected: Boolean,
    bgColor: Color,
    fgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .border(2.dp, borderColor, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = bgColor,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "بِسْمِ اللَّهِ",
                    color = fgColor,
                    fontFamily = ScheherazadeNew,
                    fontSize = 18.sp
                )
            }
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = BeVietnamPro,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ScriptPreviewCard(
    label: String,
    sample: String,
    selected: Boolean,
    isDownloaded: Boolean = true,
    isDownloading: Boolean = false,
    downloadProgress: Float = 0f,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit = onClick,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val containerBg = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val labelColor = when {
        selected -> MaterialTheme.colorScheme.primary
        !isDownloaded -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .border(2.dp, borderColor, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = containerBg,
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = sample,
                    color = contentColor,
                    fontFamily = ScheherazadeNew,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Text(
            text = when {
                isDownloading -> "$label ${"%.0f".format(downloadProgress * 100)}%"
                !isDownloaded -> "$label ↓"
                else -> label
            },
            style = MaterialTheme.typography.labelMedium,
            fontFamily = BeVietnamPro,
            color = labelColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        if (!isDownloaded && !isDownloading) {
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Download $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (isDownloading) {
            LinearProgressIndicator(
                progress = { downloadProgress },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontFamily = BeVietnamPro,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationPickerSheet(
    selectedId: String,
    selectedSource: ContentSource,
    isDownloading: Boolean,
    progress: Float,
    onSelectSource: (ContentSource) -> Unit,
    onSelect: (TranslationMeta) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val translationSources = remember {
        ContentSource.entries.filter { it.supportsTranslations }
    }

    val catalog = remember(selectedSource) {
        val list = CatalogRepository.translations(context, selectedSource).toMutableList()
        // Always show bundled at top
        val bundled = listOf(TranslationMeta.bundledEnglish(), TranslationMeta.bundledUrdu())
        bundled.forEach { b -> if (list.none { it.id == b.id }) list.add(0, b) }
        list.groupBy { it.langName.ifBlank { "Other" } }.toSortedMap()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text(
                    "Translation",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = BeVietnamPro),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SettingLabel("Source")
                Spacer(Modifier.height(8.dp))
                SourcePickerRow(
                    sources = translationSources,
                    selected = selectedSource,
                    onSelect = onSelectSource,
                )
                Spacer(Modifier.height(16.dp))

                if (isDownloading) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("Downloading...", style = MaterialTheme.typography.bodyMedium,
                            fontFamily = BeVietnamPro, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    catalog.forEach { (lang, translations) ->
                        item {
                            Text(lang, style = MaterialTheme.typography.labelLarge,
                                fontFamily = BeVietnamPro, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp))
                        }
                        items(translations) { meta ->
                            val isBundled = meta.id in TranslationMeta.BUNDLED
                            val isDownloaded = isBundled || TranslationRepository.isDownloaded(context, meta.id)
                            val isSelected = selectedId == meta.id

                            Surface(
                                onClick = { if (!isDownloading) onSelect(meta) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(meta.translatorName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontFamily = BeVietnamPro,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        Text(
                                            buildString {
                                                append(meta.langCode.uppercase())
                                                if (isBundled) append(" • Bundled")
                                                else if (isDownloaded) append(" • Downloaded")
                                                else append(" • Tap to download")
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = BeVietnamPro,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    when {
                                        isSelected -> Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                        !isDownloaded -> Icon(Icons.Default.Download, null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
