package com.kaizen.khushu.data.repository

import android.content.Context
import android.graphics.Typeface as AndroidTypeface
import androidx.compose.ui.text.font.FontFamily
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object QuranScriptFontRepository {
    const val UTHMANIC_HAFS = "uthmanic_hafs"

    private const val UTHMANIC_HAFS_URL =
        "https://github.com/quran/quran-android/raw/master/app/src/main/assets/fonts/hafs_newandroid.ttf"

    private val bundledScripts = setOf("uthmani", "indopak", "uthmani_simple", "imlaei")
    private val fontCache = ConcurrentHashMap<String, FontFamily>()

    fun bundledScripts(): Set<String> = bundledScripts

    fun availableScripts(context: Context): Set<String> =
        bundledScripts + if (isDownloaded(context, UTHMANIC_HAFS)) setOf(UTHMANIC_HAFS) else emptySet()

    fun isDownloaded(context: Context, script: String): Boolean = when (script) {
        UTHMANIC_HAFS -> uthmanicHafsFile(context).exists()
        else -> bundledScripts.contains(script)
    }

    suspend fun downloadUthmanicHafs(
        context: Context,
        onProgress: (Float) -> Unit = {},
    ): Boolean {
        val target = uthmanicHafsFile(context)
        val temp = File(target.parentFile, "${target.name}.part")
        target.parentFile?.mkdirs()

        val conn = URL(UTHMANIC_HAFS_URL).openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "KhushuApp/1.0")
            connectTimeout = 15_000
            readTimeout = 20_000
            connect()
        }

        if (conn.responseCode !in 200..299) return false

        val totalBytes = conn.contentLengthLong.takeIf { it > 0L }
        conn.inputStream.use { input ->
            FileOutputStream(temp).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesCopied = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    bytesCopied += read
                    if (totalBytes != null) {
                        onProgress((bytesCopied.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f))
                    }
                }
            }
        }

        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
        onProgress(1f)
        fontCache.remove(UTHMANIC_HAFS)
        return true
    }

    fun getDownloadedFontFamily(context: Context, script: String): FontFamily? = when (script) {
        UTHMANIC_HAFS -> fontCache.getOrPut(script) {
            FontFamily(AndroidTypeface.createFromFile(uthmanicHafsFile(context)))
        }.takeIf { isDownloaded(context, script) }
        else -> null
    }

    private fun uthmanicHafsFile(context: Context): File =
        File(File(context.filesDir, "fonts"), "uthmanic_hafs.ttf")
}
