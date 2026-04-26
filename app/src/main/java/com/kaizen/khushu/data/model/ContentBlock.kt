package com.kaizen.khushu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Polymorphic sealed class for Learn topic content blocks.
 *
 * JSON discriminator field is "type" (default for kotlinx.serialization).
 * Each subclass uses @SerialName matching the "type" value in JSON.
 *
 * JSON block types: heading, paragraph, ayah, hadith, callout, divider
 */
@Serializable
sealed class ContentBlock

@Serializable
@SerialName("heading")
data class HeadingBlock(
    val text: String,
) : ContentBlock()

@Serializable
@SerialName("paragraph")
data class ParagraphBlock(
    val text: String,
) : ContentBlock()

@Serializable
@SerialName("ayah")
data class AyahBlock(
    val surah: Int,
    val ayah: Int,
    val display: String,
    val verified: Boolean = false,
    val textUthmani: String? = null,
    val translationEn: String? = null,
    val tajweedMarkup: String? = null,
    val tafsirText: String? = null,
) : ContentBlock()

@Serializable
@SerialName("hadith")
data class HadithBlock(
    val collection: String,
    val number: Int,
    val display: String,
    val verified: Boolean = false,
    val textArabic: String? = null,
    val textEn: String? = null,
    val grade: String? = null,
    val narrator: String? = null,
    val chapter: String? = null,
) : ContentBlock()

@Serializable
@SerialName("callout")
data class CalloutBlock(
    val style: String = "note", // "note" | "tip" | "warning"
    val text: String,
) : ContentBlock()

@Serializable
@SerialName("divider")
class DividerBlock : ContentBlock()

@Serializable
@SerialName("arabic")
data class ArabicBlock(
    val text: String,
    val tajweed: String? = null,
    val translation: String = "",
) : ContentBlock()

// ── JSON wrapper ───────────────────────────────────────────────────────────────

@Serializable
data class TopicJson(
    val id: String,
    val title: String,
    val version: Int = 1,
    val blocks: List<ContentBlock>,
)
