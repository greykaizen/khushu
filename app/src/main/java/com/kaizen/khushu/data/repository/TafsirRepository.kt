package com.kaizen.khushu.data.repository

import android.content.Context
import com.kaizen.khushu.data.model.ContentSource
import com.kaizen.khushu.data.model.TafsirMeta
import kotlinx.serialization.json.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object TafsirRepository {
    private val cache = mutableMapOf<String, Map<Int, String>>() // "tafsirId_surah" -> ayah->text

    fun isDownloaded(context: Context, tafsirId: String, surahNumber: Int): Boolean {
        val file = File(context.filesDir, "tafsirs/${tafsirId}/${surahNumber}.json")
        return file.exists()
    }

    fun hasRenderableTafsir(context: Context, tafsirId: String, surahNumber: Int): Boolean {
        if (!isDownloaded(context, tafsirId, surahNumber)) return false
        return loadSurah(context, tafsirId, surahNumber).isNotEmpty()
    }

    suspend fun downloadSurah(
        context: Context,
        meta: TafsirMeta,
        surahNumber: Int,
        onProgress: (Float) -> Unit = {},
    ) {
        val dir = File(context.filesDir, "tafsirs/${meta.id}")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$surahNumber.json")
        cache.remove("${meta.id}_${surahNumber}")

        // Prefer the Quran.com tafsir API for supported numeric tafsir resources. It returns
        // stable verse-indexed tafsir objects that we normalize into the app's local cache shape.
        if ((meta.source == ContentSource.QF || meta.source == ContentSource.SPA5K) && meta.id.toIntOrNull() != null) {
            val quranApiUrl = "https://api.quran.com/api/v4/tafsirs/${meta.id}?chapter_number=$surahNumber"
            try {
                with(URL(quranApiUrl).openConnection() as HttpURLConnection) {
                    setRequestProperty("Accept", "application/json")
                    connect()
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val body = inputStream.bufferedReader().use { it.readText() }
                        val tafsirArray = Json.parseToJsonElement(body).jsonObject["tafsirs"]?.jsonArray
                        if (tafsirArray != null) {
                            val normalized = JsonArray(
                                tafsirArray.map { el ->
                                    val obj = el.jsonObject
                                    buildJsonObject {
                                        put(
                                            "ayah",
                                            obj["verse_number"]?.jsonPrimitive?.intOrNull
                                                ?: obj["ayah"]?.jsonPrimitive?.intOrNull
                                                ?: 0
                                        )
                                        put(
                                            "text",
                                            obj["text"]?.jsonPrimitive?.content
                                                ?: obj["tafsir"]?.jsonPrimitive?.content
                                                ?: ""
                                        )
                                    }
                                }
                            )
                            file.writeText(normalized.toString())
                            cache.remove("${meta.id}_${surahNumber}")
                            onProgress(1f)
                            return
                        }
                    }
                }
            } catch (_: Exception) {
                // Fall through to legacy static JSON download below.
            }
        }

        val url = meta.urlPattern.replace("{surah}", surahNumber.toString())
        if (url.startsWith("qf:")) return

        with(URL(url).openConnection() as HttpURLConnection) {
            connect()
            if (responseCode != HttpURLConnection.HTTP_OK) return
            val total = contentLength
            var downloaded = 0
            inputStream.use { input ->
                file.outputStream().use { out ->
                    val buf = ByteArray(8192)
                    var n = input.read(buf)
                    while (n >= 0) {
                        out.write(buf, 0, n)
                        downloaded += n
                        if (total > 0) onProgress(downloaded.toFloat() / total)
                        n = input.read(buf)
                    }
                }
            }
        }
        cache.remove("${meta.id}_${surahNumber}")
    }

    fun loadSurah(context: Context, tafsirId: String, surahNumber: Int): Map<Int, String> {
        val key = "${tafsirId}_${surahNumber}"
        cache[key]?.let { return it }
        val file = File(context.filesDir, "tafsirs/${tafsirId}/${surahNumber}.json")
        if (!file.exists()) return emptyMap()
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val arr = json.parseToJsonElement(file.readText()).jsonArray
            // spa5k format: [{ayah: 1, text: "..."}, ...]
            val map = arr.associate { el ->
                val obj = el.jsonObject
                val ayah = obj["ayah"]?.jsonPrimitive?.intOrNull
                    ?: obj["verse"]?.jsonPrimitive?.intOrNull ?: 0
                val text = obj["text"]?.jsonPrimitive?.content
                    ?: obj["tafsir"]?.jsonPrimitive?.content ?: ""
                ayah to text
            }
            cache[key] = map
            map
        } catch (_: Exception) { emptyMap() }
    }
}
