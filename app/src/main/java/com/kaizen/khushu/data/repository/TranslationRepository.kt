package com.kaizen.khushu.data.repository

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object TranslationRepository {
    private val cache = ConcurrentHashMap<String, Map<String, String>>()
    private val json = Json { ignoreUnknownKeys = true }

    fun isDownloaded(context: Context, id: String): Boolean {
        if (id == "en_20") return true
        val file = File(context.filesDir, "translations/$id.json")
        return file.exists()
    }

    suspend fun download(context: Context, id: String, url: String, onProgress: (Float) -> Unit = {}) {
        val dir = File(context.filesDir, "translations")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$id.json")

        with(URL(url).openConnection() as HttpURLConnection) {
            connect()
            if (responseCode != HttpURLConnection.HTTP_OK) return
            val totalSize = contentLength
            var downloaded = 0
            
            inputStream.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        downloaded += bytes
                        if (totalSize > 0) {
                            onProgress(downloaded.toFloat() / totalSize)
                        }
                        bytes = input.read(buffer)
                    }
                }
            }
        }
    }

    fun load(context: Context, id: String): Map<String, String> {
        val cached = cache[id]
        if (cached != null) return cached

        val content = if (id == "en_20") {
            context.assets.open("translations/en_20.json").bufferedReader().use { it.readText() }
        } else {
            val file = File(context.filesDir, "translations/$id.json")
            if (!file.exists()) return emptyMap()
            file.readText()
        }

        val map = parseJson(content)
        cache[id] = map
        return map
    }

    private fun parseJson(content: String): Map<String, String> {
        val root = json.parseToJsonElement(content).jsonObject
        // quran.com format: {"1:1": "text", "1:2": "text", ...}
        if (root.containsKey("1:1") || root.containsKey("2:1")) {
            return root.mapValues { it.value.jsonPrimitive.content }
        }
        // Fawaz format: {"quran": [{"chapter":1,"verse":1,"text":"..."}]}
        val quran = root["quran"]?.jsonArray ?: return emptyMap()
        val map = mutableMapOf<String, String>()
        for (item in quran) {
            val obj = item.jsonObject
            val surah = obj["chapter"]?.jsonPrimitive?.content ?: continue
            val ayah = obj["verse"]?.jsonPrimitive?.content ?: continue
            val text = obj["text"]?.jsonPrimitive?.content ?: continue
            map["$surah:$ayah"] = text
        }
        return map
    }

    fun getTranslation(map: Map<String, String>, surah: Int, ayah: Int): String? {
        return map["$surah:$ayah"]
    }

    fun getCachedMap(id: String): Map<String, String>? {
        return cache[id]
    }
}
