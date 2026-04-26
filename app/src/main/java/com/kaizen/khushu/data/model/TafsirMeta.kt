package com.kaizen.khushu.data.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull

data class TafsirMeta(
    val id: String,
    val name: String,
    val author: String,
    val language: String,
    val langCode: String,
    val source: ContentSource,
    /** URL pattern with {surah} placeholder, or "qf:{id}" for QF API */
    val urlPattern: String,
) {
    companion object {
        fun fromJson(obj: JsonObject, source: ContentSource): TafsirMeta {
            val id = obj["id"]?.jsonPrimitive?.let {
                it.intOrNull?.toString() ?: it.content
            } ?: ""
            val lang = obj["language"]?.jsonPrimitive?.content
                ?: obj["language_name"]?.jsonPrimitive?.content ?: ""
            val pattern = obj["url_pattern"]?.jsonPrimitive?.content
                ?: if (source == ContentSource.QF) "qf:tafsir:$id" else ""
            return TafsirMeta(
                id = id,
                name = obj["name"]?.jsonPrimitive?.content ?: id,
                author = obj["author"]?.jsonPrimitive?.content
                    ?: obj["author_name"]?.jsonPrimitive?.content ?: "",
                language = lang.replaceFirstChar { it.uppercase() },
                langCode = lang.take(2),
                source = source,
                urlPattern = pattern,
            )
        }
    }
}