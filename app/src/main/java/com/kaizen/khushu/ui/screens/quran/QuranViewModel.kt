package com.kaizen.khushu.ui.screens.quran

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.model.SurahMeta
import com.kaizen.khushu.data.repository.QuranRepository
import com.kaizen.khushu.data.repository.LearnRepository
import com.kaizen.khushu.data.repository.VerseMeta
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.data.model.ContentSource
import com.kaizen.khushu.data.model.ReflectionPost
import com.kaizen.khushu.data.model.TafsirMeta
import com.kaizen.khushu.data.repository.CatalogRepository
import com.kaizen.khushu.data.repository.TafsirRepository
import com.kaizen.khushu.data.repository.QuranReflectRepository
import com.kaizen.khushu.data.repository.QuranScriptFontRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    val chapters = mutableStateOf<List<SurahMeta>>(emptyList())
    val currentAyahs = mutableStateOf<List<Pair<Int, String>>>(emptyList())
    val currentTranslation = mutableStateOf<Map<Int, String>>(emptyMap())
    val scriptMap = mutableStateOf<Map<String, String>>(emptyMap())
    val verseMeta = mutableStateOf<Map<String, VerseMeta>>(emptyMap())
    val isLoading = mutableStateOf(false)

    val tafsirText = mutableStateOf<Map<Int, String>>(emptyMap())
    val isTafsirDownloading = mutableStateOf(false)
    val tafsirDownloadProgress = mutableStateOf(0f)
    val downloadingTafsirId = mutableStateOf<String?>(null)

    // Reflections: verseKey ("surah:ayah") -> posts
    val reflections = mutableStateOf<Map<String, List<ReflectionPost>>>(emptyMap())
    val reflectionsLoading = mutableStateOf<Set<String>>(emptySet())

    fun loadTafsirIfEnabled(context: android.content.Context, settings: UserSettings, surahNumber: Int) {
        if (!settings.showTafsir || settings.selectedTafsirId.isBlank()) {
            tafsirText.value = emptyMap()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val requestedSource = try { ContentSource.valueOf(settings.selectedTafsirSource) }
                catch (_: Exception) { ContentSource.SPA5K }
            val source = if (requestedSource == ContentSource.QF) ContentSource.SPA5K else requestedSource
            val catalog = CatalogRepository.tafsirs(context, source)
            val meta = catalog.find { it.id == settings.selectedTafsirId } ?: run {
                withContext(Dispatchers.Main) { tafsirText.value = emptyMap() }
                return@launch
            }

            if (!TafsirRepository.hasRenderableTafsir(context, meta.id, surahNumber)) {
                isTafsirDownloading.value = true
                TafsirRepository.downloadSurah(context, meta, surahNumber) { p ->
                    tafsirDownloadProgress.value = p
                }
                isTafsirDownloading.value = false
            }
            val loadedTafsir = TafsirRepository.loadSurah(context, meta.id, surahNumber)
            withContext(Dispatchers.Main) {
                tafsirText.value = loadedTafsir
            }
        }
    }

    fun downloadTafsirForSurah(
        context: android.content.Context,
        meta: TafsirMeta,
        surahNumber: Int,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (TafsirRepository.hasRenderableTafsir(context, meta.id, surahNumber)) {
                withContext(Dispatchers.Main) { onComplete() }
                return@launch
            }

            withContext(Dispatchers.Main) {
                isTafsirDownloading.value = true
                tafsirDownloadProgress.value = 0f
                downloadingTafsirId.value = meta.id
            }

            try {
                TafsirRepository.downloadSurah(context, meta, surahNumber) { progress ->
                    tafsirDownloadProgress.value = progress
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isTafsirDownloading.value = false
                    tafsirDownloadProgress.value = 0f
                    downloadingTafsirId.value = null
                }
            }

            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    fun loadVerseMeta(context: android.content.Context) {
        if (verseMeta.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val map = QuranRepository.getVerseMeta(context)
            withContext(Dispatchers.Main) {
                verseMeta.value = map
            }
        }
    }

    fun loadChapters() {
        if (chapters.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val list = QuranRepository.getChapters(getApplication())
            withContext(Dispatchers.Main) {
                chapters.value = list
            }
        }
    }

    fun loadSurah(surahNumber: Int, translationId: String) {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val ayahs = QuranRepository.getAyahs(getApplication(), surahNumber)
            val translation = QuranRepository.getTranslation(getApplication(), surahNumber, translationId)
            withContext(Dispatchers.Main) {
                currentAyahs.value = ayahs
                currentTranslation.value = translation
                isLoading.value = false
            }
        }
    }

    fun loadTranslation(context: android.content.Context, surahNumber: Int, translationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val translation = QuranRepository.getTranslation(context, surahNumber, translationId)
            withContext(Dispatchers.Main) {
                currentTranslation.value = translation
            }
        }
    }

    fun loadScript(context: android.content.Context, script: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (script == "uthmani" || script == QuranScriptFontRepository.UTHMANIC_HAFS) {
                withContext(Dispatchers.Main) {
                    scriptMap.value = emptyMap()
                }
            } else {
                val map = LearnRepository.getScriptMap(context, script)
                withContext(Dispatchers.Main) {
                    scriptMap.value = map
                }
            }
        }
    }

    fun loadReflections(surah: Int, ayah: Int) {
        val key = "$surah:$ayah"
        // Already loaded or in flight
        if (QuranReflectRepository.isLoaded(surah, ayah)) {
            reflections.value = reflections.value + (key to QuranReflectRepository.getCached(surah, ayah))
            return
        }
        if (reflectionsLoading.value.contains(key)) return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                reflectionsLoading.value = reflectionsLoading.value + key
            }
            val posts = QuranReflectRepository.fetch(surah, ayah)
            withContext(Dispatchers.Main) {
                reflections.value = reflections.value + (key to posts)
                reflectionsLoading.value = reflectionsLoading.value - key
            }
        }
    }
}
