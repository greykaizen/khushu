package com.kaizen.khushu.data.repository

import android.content.Context
import com.kaizen.khushu.data.model.ContentSource
import com.kaizen.khushu.data.model.ReciterMeta
import com.kaizen.khushu.data.model.TafsirMeta
import com.kaizen.khushu.data.model.TranslationMeta
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap

object CatalogRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val translationCache = ConcurrentHashMap<ContentSource, List<TranslationMeta>>()
    private val tafsirCache = ConcurrentHashMap<ContentSource, List<TafsirMeta>>()
    private val reciterCache = ConcurrentHashMap<ContentSource, List<ReciterMeta>>()

    fun translations(context: Context, source: ContentSource): List<TranslationMeta> {
        translationCache[source]?.let { return it }
        val file = when (source) {
            ContentSource.QF -> "catalogs/qf_translations.json"
            ContentSource.FAWAZ -> "catalogs/fawaz_translations.json"
            ContentSource.QURANENC -> "catalogs/quranenc_translations.json"
            else -> return emptyList()
        }
        return try {
            val raw = context.assets.open(file).bufferedReader().use { it.readText() }
            val arr = json.parseToJsonElement(raw).jsonArray
            val list = when (source) {
                ContentSource.QF -> arr.map { TranslationMeta.fromQfJson(it.jsonObject, source) }
                ContentSource.FAWAZ -> arr.map { TranslationMeta.fromFawazJson(it.jsonObject, source) }
                ContentSource.QURANENC -> arr.map { TranslationMeta.fromQuranEncJson(it.jsonObject, source) }
                else -> emptyList()
            }.filter { it.id.isNotBlank() }
            translationCache[source] = list
            list
        } catch (_: Exception) { emptyList() }
    }

    fun tafsirs(context: Context, source: ContentSource): List<TafsirMeta> {
        tafsirCache[source]?.let { return it }
        val file = when (source) {
            ContentSource.QF -> "catalogs/qf_tafsirs.json"
            ContentSource.SPA5K -> "catalogs/spa5k_tafsirs.json"
            else -> return emptyList()
        }
        return try {
            val raw = context.assets.open(file).bufferedReader().use { it.readText() }
            val arr = json.parseToJsonElement(raw).jsonArray
            val list = arr.map { TafsirMeta.fromJson(it.jsonObject, source) }.filter { it.id.isNotBlank() }
            tafsirCache[source] = list
            list
        } catch (_: Exception) { emptyList() }
    }

    fun reciters(context: Context, source: ContentSource): List<ReciterMeta> {
        reciterCache[source]?.let { return it }
        val file = when (source) {
            ContentSource.QF -> "catalogs/qf_reciters.json"
            ContentSource.MP3QURAN -> "catalogs/mp3quran_reciters.json"
            ContentSource.EVERYAYAH -> return ReciterMeta.everyayahDefaults()
            else -> return emptyList()
        }
        return try {
            val raw = context.assets.open(file).bufferedReader().use { it.readText() }
            val arr = json.parseToJsonElement(raw).jsonArray
            val list = when (source) {
                ContentSource.QF -> arr.map { ReciterMeta.fromQfJson(it.jsonObject) }
                ContentSource.MP3QURAN -> arr.map { ReciterMeta.fromMp3QuranJson(it.jsonObject) }
                else -> emptyList()
            }.filter { it.id.isNotBlank() }
            reciterCache[source] = list
            list
        } catch (_: Exception) { emptyList() }
    }

    fun clearCache() {
        translationCache.clear(); tafsirCache.clear(); reciterCache.clear()
    }
}