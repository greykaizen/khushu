package com.kaizen.khushu.data.repository

import android.content.Context
import com.kaizen.khushu.data.model.SurahMeta
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
import java.util.concurrent.ConcurrentHashMap

data class VerseMeta(
    val juz: Int,      // 1-30
    val hizb: Int,     // 1-240 (hizbQuarter — each hizb = 4 quarters)
    val rub: Int,      // 1-4 (quarter within the hizb)
    val manzil: Int,   // 1-7
    val ruku: Int,     // sequential ruku number within the Quran
    val sajda: String? // null / "obligatory" / "recommended"
)

object QuranRepository {
    private var chaptersCache: List<SurahMeta>? = null
    private var tajweedMap: Map<String, String>? = null
    private var uthmaniMap: Map<String, String>? = null
    private var verseMetaMap: Map<String, VerseMeta>? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun getVerseMeta(context: Context): Map<String, VerseMeta> {
        verseMetaMap?.let { return it }
        return try {
            val raw = context.assets.open("quran/verse_meta.json").bufferedReader().use { it.readText() }
            val obj = json.parseToJsonElement(raw).jsonObject
            val map = obj.entries.associate { (key, value) ->
                val v = value.jsonObject
                key to VerseMeta(
                    juz    = v["juz"]?.jsonPrimitive?.intOrNull ?: 1,
                    hizb   = v["hizb"]?.jsonPrimitive?.intOrNull ?: 0,
                    rub    = v["rub"]?.jsonPrimitive?.intOrNull ?: 0,
                    manzil = v["manzil"]?.jsonPrimitive?.intOrNull ?: 0,
                    ruku   = v["ruku"]?.jsonPrimitive?.intOrNull ?: 0,
                    sajda  = v["sajda"]?.jsonPrimitive?.contentOrNull,
                )
            }
            verseMetaMap = map
            map
        } catch (_: Exception) { emptyMap() }
    }

    fun getChapters(context: Context): List<SurahMeta> {
        chaptersCache?.let { return it }
        val raw = try {
            context.assets.open("quran/chapters.json").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return emptyList()
        }
        return try {
            val list = json.decodeFromString<List<SurahMeta>>(raw)
            chaptersCache = list
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadTajweed(context: Context): Map<String, String> {
        tajweedMap?.let { return it }
        val raw = try {
            context.assets.open("quran/uthmani_tajweed.json").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return emptyMap()
        }
        return try {
            val map = json.decodeFromString<Map<String, String>>(raw)
            tajweedMap = map
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun loadUthmani(context: Context): Map<String, String> {
        uthmaniMap?.let { return it }
        val raw = try {
            context.assets.open("quran/uthmani.json").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return emptyMap()
        }
        return try {
            val map = json.decodeFromString<Map<String, String>>(raw)
            uthmaniMap = map
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun getAyahs(context: Context, surahNumber: Int): List<Pair<Int, String>> {
        val tajweed = loadTajweed(context)
        val uthmani = loadUthmani(context)
        
        val chapters = getChapters(context)
        val surah = chapters.find { it.id == surahNumber } ?: return emptyList()
        
        val result = mutableListOf<Pair<Int, String>>()
        for (i in 1..surah.versesCount) {
            val key = "$surahNumber:$i"
            val text = tajweed[key] ?: uthmani[key] ?: ""
            result.add(i to text)
        }
        return result
    }

    fun getTranslation(context: Context, surahNumber: Int, translationId: String): Map<Int, String> {
        val fullMap = TranslationRepository.load(context, translationId)
        if (fullMap.isEmpty()) return emptyMap()
        
        val result = mutableMapOf<Int, String>()
        val prefix = "$surahNumber:"
        fullMap.forEach { (key, text) ->
            if (key.startsWith(prefix)) {
                val ayahNum = key.substringAfter(":").toIntOrNull()
                if (ayahNum != null) {
                    result[ayahNum] = text
                }
            }
        }
        return result
    }
}
