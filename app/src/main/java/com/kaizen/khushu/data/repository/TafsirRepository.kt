package com.kaizen.khushu.data.repository

import android.content.Context
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

    suspend fun downloadSurah(
        context: Context,
        meta: TafsirMeta,
        surahNumber: Int,
        onProgress: (Float) -> Unit = {},
    ) {
        val url = meta.urlPattern.replace("{surah}", surahNumber.toString())
        if (url.startsWith("qf:")) return  // QF tafsir: fetch via QF API (future)

        val dir = File(context.filesDir, "tafsirs/${meta.id}")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$surahNumber.json")

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