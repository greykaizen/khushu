package com.kaizen.khushu.data.repository

import com.kaizen.khushu.data.model.ReflectionPost
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.booleanOrNull
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Fetches public reflection posts from Quran Reflect (quranreflect.com).
 *
 * The public API at quranreflect.com returns featured/public posts without
 * authentication for read access. This keeps the app fully private — we only
 * make outbound GET requests and never send user data.
 *
 * Cache: in-memory LRU-style map keyed by "surah:ayah". Cleared on process death.
 */
object QuranReflectRepository {

    private val BASE = "https://quranreflect.com"
    private val json = Json { ignoreUnknownKeys = true }

    // Cache: verseKey -> posts. Bounded at 100 entries.
    private val cache = ConcurrentHashMap<String, List<ReflectionPost>>()
    private val inFlight = ConcurrentHashMap<String, Boolean>()

    /**
     * Returns cached posts immediately (may be empty), and fetches from network
     * if not yet cached.
     */
    fun getCached(surah: Int, ayah: Int): List<ReflectionPost> =
        cache["$surah:$ayah"] ?: emptyList()

    fun isLoaded(surah: Int, ayah: Int): Boolean =
        cache.containsKey("$surah:$ayah")

    /**
     * Fetches reflections for [surah]:[ayah] from the Quran Reflect public API.
     * Returns empty list on error (silent failure — reflections are optional content).
     */
    suspend fun fetch(surah: Int, ayah: Int): List<ReflectionPost> {
        val key = "$surah:$ayah"
        cache[key]?.let { return it }
        if (inFlight[key] == true) return emptyList()

        inFlight[key] = true
        return try {
            val posts = fetchFromNetwork(surah, ayah)
            // Evict oldest if cache too large
            if (cache.size >= 100) {
                cache.keys.take(20).forEach { cache.remove(it) }
            }
            cache[key] = posts
            posts
        } catch (_: Exception) {
            cache[key] = emptyList()
            emptyList()
        } finally {
            inFlight.remove(key)
        }
    }

    private fun fetchFromNetwork(surah: Int, ayah: Int): List<ReflectionPost> {
        val verseKey = "$surah:$ayah"
        // Primary: verse-specific featured posts
        val url = URL("$BASE/posts.json?ayah=$verseKey&featured=true&page=1")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "KhushuApp/1.0")
            connectTimeout = 8000
            readTimeout = 10000
            connect()
        }

        if (conn.responseCode != HttpURLConnection.HTTP_OK) return emptyList()

        val body = conn.inputStream.bufferedReader().use { it.readText() }
        return parseResponse(body, verseKey)
    }

    private fun parseResponse(body: String, verseKey: String): List<ReflectionPost> {
        val root = json.parseToJsonElement(body).jsonObject
        // Response shape: { "posts": [ { ... } ] } or top-level array
        val arr = root["posts"]?.jsonArray
            ?: root["data"]?.jsonArray
            ?: json.parseToJsonElement(body).jsonArray

        return arr.mapNotNull { el ->
            try {
                val obj = el.jsonObject
                val user = obj["user"]?.jsonObject
                val bodyHtml = obj["body"]?.jsonPrimitive?.content ?: return@mapNotNull null
                ReflectionPost(
                    id = obj["id"]?.jsonPrimitive?.intOrNull ?: 0,
                    verseKey = verseKey,
                    body = bodyHtml,
                    bodyPlain = stripHtml(bodyHtml),
                    userName = user?.get("name")?.jsonPrimitive?.content
                        ?: obj["user_name"]?.jsonPrimitive?.content ?: "Anonymous",
                    userHandle = user?.get("username")?.jsonPrimitive?.content
                        ?: obj["username"]?.jsonPrimitive?.content ?: "",
                    likeCount = obj["likes_count"]?.jsonPrimitive?.intOrNull
                        ?: obj["likes"]?.jsonPrimitive?.intOrNull ?: 0,
                    reflectionCount = obj["comments_count"]?.jsonPrimitive?.intOrNull ?: 0,
                    isVerified = user?.get("is_verified")?.jsonPrimitive?.booleanOrNull ?: false,
                    publishedAt = obj["created_at"]?.jsonPrimitive?.content
                        ?: obj["published_at"]?.jsonPrimitive?.content ?: "",
                )
            } catch (_: Exception) { null }
        }
    }

    private fun stripHtml(html: String): String =
        html.replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()

    fun clearCache() = cache.clear()
}
