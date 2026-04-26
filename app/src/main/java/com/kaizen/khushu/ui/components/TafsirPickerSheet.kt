package com.kaizen.khushu.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.data.model.ContentSource
import com.kaizen.khushu.data.model.TafsirMeta
import com.kaizen.khushu.data.repository.CatalogRepository
import com.kaizen.khushu.data.repository.TafsirRepository
import com.kaizen.khushu.ui.theme.BeVietnamPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirPickerSheet(
    selectedTafsirId: String,
    selectedSource: ContentSource,
    currentSurah: Int,
    isDownloading: Boolean,
    progress: Float,
    onSelectSource: (ContentSource) -> Unit,
    onSelect: (TafsirMeta) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val tafsirSources = remember { ContentSource.entries.filter { it.supportsTafsir } }

    val catalog = remember(selectedSource) {
        CatalogRepository.tafsirs(context, selectedSource)
            .groupBy { it.language.ifBlank { "Other" } }
            .toSortedMap()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text("Tafsir", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = BeVietnamPro),
                    modifier = Modifier.padding(bottom = 12.dp))

                SettingLabel("Source")
                Spacer(Modifier.height(8.dp))
                SourcePickerRow(sources = tafsirSources, selected = selectedSource, onSelect = onSelectSource)
                Spacer(Modifier.height(16.dp))

                if (isDownloading) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("Downloading Surah $currentSurah tafsir...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = BeVietnamPro, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    catalog.forEach { (lang, tafsirs) ->
                        item {
                            Text(lang, style = MaterialTheme.typography.labelLarge,
                                fontFamily = BeVietnamPro, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp))
                        }
                        items(tafsirs) { meta ->
                            val isDownloaded = TafsirRepository.isDownloaded(context, meta.id, currentSurah)
                            val isSelected = selectedTafsirId == meta.id

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
                                        Text(meta.name, style = MaterialTheme.typography.bodyLarge,
                                            fontFamily = BeVietnamPro,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        Text(
                                            buildString {
                                                if (meta.author.isNotBlank()) append(meta.author)
                                                append(" • ")
                                                if (isDownloaded) append("Downloaded")
                                                else append("Tap to download this surah")
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