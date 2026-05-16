package com.kaizen.khushu.data.repository

import android.content.Context
import com.kaizen.khushu.data.model.ContentBlock
import com.kaizen.khushu.data.model.LearnSection
import com.kaizen.khushu.data.model.LearnTopic
import com.kaizen.khushu.data.model.TopicJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.ConcurrentHashMap

object LearnRepository {
    private val blockCache = ConcurrentHashMap<String, List<ContentBlock>>()
    private val tajweedCache = ConcurrentHashMap<String, String>()
    private val scriptCache = ConcurrentHashMap<String, Map<String, String>>()

    fun getTajweedMap(context: Context): Map<String, String> {
        if (tajweedCache.isNotEmpty()) return tajweedCache
        
        val raw = try {
            context.assets.open("quran/uthmani_tajweed.json")
                .bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return emptyMap()
        }

        return try {
            val map = Json.decodeFromString<Map<String, String>>(raw)
            tajweedCache.putAll(map)
            tajweedCache
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun getScriptMap(context: Context, script: String): Map<String, String> {
        scriptCache[script]?.let { return it }
        val filename = when (script) {
            QuranScriptFontRepository.UTHMANIC_HAFS -> "uthmani.json"
            "indopak"       -> "indopak.json"
            "uthmani_simple"-> "uthmani_simple.json"
            "imlaei"        -> "imlaei.json"
            else            -> "uthmani.json"  // default
        }
        return try {
            val content = context.assets.open("quran/$filename").bufferedReader().use { it.readText() }
            val map = Json.parseToJsonElement(content).jsonObject
                .mapValues { it.value.jsonPrimitive.content }
            scriptCache[script] = map
            map
        } catch (_: Exception) { emptyMap() }
    }

    fun getSections(context: Context): List<LearnSection> {
        val quranTopics = QuranRepository.getChapters(context).map { surah ->
            LearnTopic(
                id = "quran_surah_${surah.id}",
                title = "${surah.id} ${surah.nameSimple}",
                arabicText = surah.nameArabic,
                translations = mapOf("en" to "")
            )
        }

        return listOf(
            LearnSection(
                id = "quran",
                sectionTitle = "Holy Quran",
                color = 0xFF43A047L,
                topics = quranTopics
            ),
            LearnSection(
                id = "hadith",
                sectionTitle = "Prophetic Hadith",
                color = 0xFF673AB7L,
                topics = com.kaizen.khushu.data.model.BUNDLED_HADITH_BOOKS.map { book ->
                    LearnTopic(
                        id = "hadith_book_${book.id}",
                        title = book.name,
                        arabicText = "",
                        translations = mapOf("en" to "")
                    )
                }
            ),
            LearnSection(
                id = "foundations",
                sectionTitle = "Foundations",
                color = 0xFF2A4B7CL,
                topics = listOf(
                    LearnTopic(id = "foundations_intention", title = "The Role of Intention (Niyyah)", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "foundations_sincerity", title = "Sincerity (Ikhlas) in Worship", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "foundations_knowledge", title = "Seeking Islamic Knowledge", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "foundations_pillars", title = "The Five Pillars of Islam", arabicText = "", translations = mapOf("en" to ""))
                )
            ),
            LearnSection(
                id = "purification",
                sectionTitle = "Purification",
                color = 0xFF357B83L,
                topics = listOf(
                    LearnTopic(id = "purification_types", title = "Types of Purification in Islam", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "purification_istinja", title = "Istinja and Istijmar", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "wudu_step_by_step", title = "Step-by-Step Wudu", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "wudu_invalidators", title = "What Breaks Wudu", arabicText = "", translations = mapOf("en" to ""))
                )
            ),
            LearnSection(
                id = "prayer",
                sectionTitle = "The Prayer",
                color = 0xFF2D5A4CL,
                topics = listOf(
                    LearnTopic(id = "salah_obligation", title = "The Obligation of Salah", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "salah_times", title = "The Five Prayer Times", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "salah_rakaat", title = "Rakaat Count for Each Prayer", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "salah_khushoo", title = "Achieving Khushoo in Salah", arabicText = "", translations = mapOf("en" to ""))
                )
            ),
            LearnSection(
                id = "duas_adhkar",
                sectionTitle = "Duas & Adhkar",
                color = 0xFFA87B61L,
                topics = listOf(
                    LearnTopic(id = "surah_fatiha", title = "Surah Al-Fatiha", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "dua_morning_evening", title = "Morning and Evening Adhkar", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "dua_before_sleep", title = "Duas Before Sleep", arabicText = "", translations = mapOf("en" to "")),
                    LearnTopic(id = "duas_after_fard", title = "Duas After Obligatory Prayer", arabicText = "", translations = mapOf("en" to ""))
                )
            )
        )
    }

    private val blockJson = Json { ignoreUnknownKeys = true }

    suspend fun getBlocks(topicId: String, context: Context): List<ContentBlock> {
        blockCache[topicId]?.let { return it }

        val raw = try {
            context.assets.open("learn/$topicId.json")
                .bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return emptyList()
        }

        return try {
            blockJson.decodeFromString<TopicJson>(raw).blocks
        } catch (_: Exception) {
            emptyList()
        }.also { blockCache[topicId] = it }
    }
}
