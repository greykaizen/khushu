package com.kaizen.khushu.data.repository

import android.content.Context
import com.kaizen.khushu.data.model.HadithBook
import com.kaizen.khushu.data.model.HadithSection
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap

object HadithRepository {
    private val bookCache = ConcurrentHashMap<String, JsonObject>()
    private val json = Json { ignoreUnknownKeys = true }

    private fun loadBook(context: Context, bookId: String): JsonObject {
        bookCache[bookId]?.let { return it }
        val filename = "en.$bookId.json"
        return try {
            val raw = context.assets.open("hadith/$filename").bufferedReader().use { it.readText() }
            val obj = json.parseToJsonElement(raw).jsonObject
            bookCache[bookId] = obj
            obj
        } catch (_: Exception) {
            JsonObject(emptyMap())
        }
    }

    private val arabicCache = ConcurrentHashMap<String, Map<Int, String>>()

    fun getArabicBook(context: Context, bookId: String): Map<Int, String> {
        arabicCache[bookId]?.let { return it }
        val filename = "ar.$bookId.json"
        return try {
            val raw = context.assets.open("hadith/$filename").bufferedReader().use { it.readText() }
            val obj = json.parseToJsonElement(raw).jsonObject
            val hadiths = obj["hadiths"]?.jsonArray ?: return emptyMap()
            val map = hadiths.associate { el ->
                val h = el.jsonObject
                val num = h["hadithnumber"]?.jsonPrimitive?.intOrNull ?: 0
                val text = h["text"]?.jsonPrimitive?.content ?: ""
                num to text
            }
            arabicCache[bookId] = map
            map
        } catch (_: Exception) { emptyMap() }
    }

    fun getSections(context: Context, bookId: String): List<HadithSection> {
        val book = loadBook(context, bookId)
        val metadata = book["metadata"]?.jsonObject ?: return emptyList()
        val sections = metadata["sections"]?.jsonObject ?: return emptyList()
        
        return sections.entries.mapNotNull { (key, value) ->
            val num = key.toIntOrNull() ?: return@mapNotNull null
            val title = value.jsonPrimitive.content
            if (title.isBlank()) return@mapNotNull null
            HadithSection(num, title)
        }.sortedBy { it.number }
    }

    fun getHadiths(context: Context, bookId: String, sectionNumber: Int): List<JsonObject> {
        val book = loadBook(context, bookId)
        val hadiths = book["hadiths"]?.jsonArray ?: return emptyList()
        
        return hadiths.map { it.jsonObject }.filter { 
            it["reference"]?.jsonObject?.get("book")?.jsonPrimitive?.intOrNull == sectionNumber
        }
    }
}
