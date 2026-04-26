package com.kaizen.khushu.data.repository

import android.content.Context
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import com.kaizen.khushu.data.repository.CatalogRepository

object QuranAudioRepository {
    private val manifestCache = ConcurrentHashMap<String, Map<Int, String>>()
    private val json = Json { ignoreUnknownKeys = true }

    fun getManifest(context: Context, reciterId: String): Map<Int, String> {
        manifestCache[reciterId]?.let { return it }
        
        val raw = try {
            context.assets.open("quran/audio/$reciterId.json")
                .bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return emptyMap()
        }

        return try {
            val stringMap = json.decodeFromString<Map<String, String>>(raw)
            val intMap = stringMap.mapKeys { it.key.toInt() }
            manifestCache[reciterId] = intMap
            intMap
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun getUrl(context: Context, reciterId: String, surahNumber: Int): String? {
        var reciter = CatalogRepository.reciters(context, com.kaizen.khushu.data.model.ContentSource.MP3QURAN).find { it.id == reciterId }
        if (reciter == null) {
            reciter = CatalogRepository.reciters(context, com.kaizen.khushu.data.model.ContentSource.QF).find { it.id == reciterId }
        }
        if (reciter == null) {
            reciter = CatalogRepository.reciters(context, com.kaizen.khushu.data.model.ContentSource.EVERYAYAH).find { it.id == reciterId }
        }
        if (reciter == null) {
            reciter = com.kaizen.khushu.data.model.AVAILABLE_RECITERS.find { it.id == reciterId }
        }
        
        if (reciter != null && reciter.audioUrlPattern.isNotBlank()) {
            val surahPadded = surahNumber.toString().padStart(3, '0')
            return reciter.audioUrlPattern
                .replace("{surah}", surahNumber.toString())
                .replace("{surah_padded}", surahPadded)
        }

        val manifest = getManifest(context, reciterId)
        return manifest[surahNumber]
    }
}
