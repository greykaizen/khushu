package com.kaizen.khushu.data.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull

data class TranslationMeta(
    val id: String,
    val langCode: String,
    val langName: String,
    val translatorName: String,
    val isRtl: Boolean,
    val downloadUrl: String,
    val sizeKb: Int,
    val source: ContentSource,
) {
    companion object {
        /** Bundled translations always available without download */
        val BUNDLED = setOf("en_20", "ur_54")

        fun fromFawazJson(obj: JsonObject, source: ContentSource): TranslationMeta {
            val id = obj["id"]?.jsonPrimitive?.content ?: ""
            val lang = obj["language"]?.jsonPrimitive?.content ?: ""
            val langIso = obj["language_iso"]?.jsonPrimitive?.content ?: lang.take(3)
            val direction = obj["direction"]?.jsonPrimitive?.content ?: "ltr"
            val url = obj["download_url"]?.jsonPrimitive?.content ?: ""
            return TranslationMeta(
                id = id,
                langCode = langIso,
                langName = lang.replaceFirstChar { it.uppercase() },
                translatorName = obj["author"]?.jsonPrimitive?.content
                    ?: obj["name"]?.jsonPrimitive?.content ?: id,
                isRtl = direction == "rtl",
                downloadUrl = url,
                sizeKb = 0,   // fawazahmed0 doesn't expose size; estimate after download
                source = source,
            )
        }

        fun fromQfJson(obj: JsonObject, source: ContentSource): TranslationMeta {
            val id = obj["id"]?.jsonPrimitive?.intOrNull?.toString()
                ?: obj["slug"]?.jsonPrimitive?.content ?: ""
            val lang = obj["language_name"]?.jsonPrimitive?.content ?: ""
            val slug = obj["slug"]?.jsonPrimitive?.content ?: id
            return TranslationMeta(
                id = id,
                langCode = lang.take(2),
                langName = lang.replaceFirstChar { it.uppercase() },
                translatorName = obj["author_name"]?.jsonPrimitive?.content
                    ?: obj["name"]?.jsonPrimitive?.content ?: id,
                isRtl = lang in setOf("arabic", "urdu", "persian", "pashto"),
                // QF download URL: per-chapter fetch at runtime using QF API + reciter ID
                downloadUrl = "qf:translation:$id",
                sizeKb = 0,
                source = source,
            )
        }

        fun fromQuranEncJson(obj: JsonObject, source: ContentSource): TranslationMeta {
            val id = obj["id"]?.jsonPrimitive?.content ?: ""
            val langIso = obj["language"]?.jsonPrimitive?.content ?: ""  // QuranEnc "language" is ISO code
            val direction = obj["direction"]?.jsonPrimitive?.content ?: "ltr"
            val surahPattern = obj["url_pattern"]?.jsonPrimitive?.content ?: ""
            return TranslationMeta(
                id = id,
                langCode = langIso,
                langName = isoToLanguageName(langIso),
                translatorName = obj["author"]?.jsonPrimitive?.content
                    ?: obj["name"]?.jsonPrimitive?.content ?: id,
                isRtl = direction == "rtl",
                downloadUrl = surahPattern,
                sizeKb = 0,
                source = source,
            )
        }

        /** Maps ISO 639-1/2 codes to display names. Covers all codes present in QuranEnc catalog. */
        private fun isoToLanguageName(iso: String): String = when (iso.lowercase()) {
            "aa" -> "Afar"; "ab" -> "Abkhazian"; "af" -> "Afrikaans"; "ak" -> "Akan"
            "am" -> "Amharic"; "ar" -> "Arabic"; "as" -> "Assamese"; "az" -> "Azerbaijani"
            "ba" -> "Bashkir"; "be" -> "Belarusian"; "bg" -> "Bulgarian"; "bn" -> "Bengali"
            "bo" -> "Tibetan"; "bs" -> "Bosnian"; "ca" -> "Catalan"; "ceb" -> "Cebuano"
            "cs" -> "Czech"; "cy" -> "Welsh"; "da" -> "Danish"; "de" -> "German"
            "dv" -> "Divehi"; "el" -> "Greek"; "en" -> "English"; "es" -> "Spanish"
            "et" -> "Estonian"; "eu" -> "Basque"; "fa" -> "Persian"; "fi" -> "Finnish"
            "fr" -> "French"; "fy" -> "Frisian"; "ga" -> "Irish"; "gl" -> "Galician"
            "gu" -> "Gujarati"; "ha" -> "Hausa"; "he" -> "Hebrew"; "hi" -> "Hindi"
            "hr" -> "Croatian"; "ht" -> "Haitian Creole"; "hu" -> "Hungarian"; "hy" -> "Armenian"
            "id" -> "Indonesian"; "is" -> "Icelandic"; "it" -> "Italian"; "ja" -> "Japanese"
            "jv" -> "Javanese"; "ka" -> "Georgian"; "kk" -> "Kazakh"; "km" -> "Khmer"
            "kn" -> "Kannada"; "ko" -> "Korean"; "ku" -> "Kurdish"; "ky" -> "Kyrgyz"
            "la" -> "Latin"; "ln" -> "Lingala"; "lo" -> "Lao"; "lt" -> "Lithuanian"
            "lv" -> "Latvian"; "mg" -> "Malagasy"; "mi" -> "Maori"; "mk" -> "Macedonian"
            "ml" -> "Malayalam"; "mn" -> "Mongolian"; "mr" -> "Marathi"; "ms" -> "Malay"
            "mt" -> "Maltese"; "my" -> "Burmese"; "mdh" -> "Maguindanaon"; "mos" -> "Mossi"
            "ne" -> "Nepali"; "nl" -> "Dutch"; "no" -> "Norwegian"; "nqo" -> "N'Ko"
            "om" -> "Oromo"; "or" -> "Odia"; "pa" -> "Punjabi"; "pl" -> "Polish"
            "ps" -> "Pashto"; "pt" -> "Portuguese"; "rn" -> "Kirundi"; "ro" -> "Romanian"
            "ru" -> "Russian"; "rw" -> "Kinyarwanda"; "sd" -> "Sindhi"; "si" -> "Sinhala"
            "sk" -> "Slovak"; "sl" -> "Slovenian"; "sm" -> "Samoan"; "so" -> "Somali"
            "sq" -> "Albanian"; "sr" -> "Serbian"; "st" -> "Sesotho"; "su" -> "Sundanese"
            "sv" -> "Swedish"; "sw" -> "Swahili"; "ta" -> "Tamil"; "te" -> "Telugu"
            "tg" -> "Tajik"; "th" -> "Thai"; "tl" -> "Filipino"; "tr" -> "Turkish"
            "tt" -> "Tatar"; "ug" -> "Uyghur"; "uk" -> "Ukrainian"; "ur" -> "Urdu"
            "uz" -> "Uzbek"; "vi" -> "Vietnamese"; "xh" -> "Xhosa"; "yi" -> "Yiddish"
            "yo" -> "Yoruba"; "zh" -> "Chinese"; "zu" -> "Zulu"
            else -> iso.uppercase()  // fallback: show the code itself if unknown
        }

        /** Already bundled in assets/translations/ — always available offline, no download */
        fun bundledEnglish() = TranslationMeta(
            id = "en_20", langCode = "en", langName = "English",
            translatorName = "Sahih International", isRtl = false,
            downloadUrl = "", sizeKb = 380, source = ContentSource.FAWAZ
        )

        fun bundledUrdu() = TranslationMeta(
            id = "ur_54", langCode = "ur", langName = "Urdu",
            translatorName = "Maulana Fateh Muhammad Jalandhari", isRtl = true,
            downloadUrl = "", sizeKb = 420, source = ContentSource.FAWAZ
        )

        // TEMPORARY — remove in Task 5
        @Deprecated("Use CatalogRepository.translations() instead")
        val AVAILABLE_TRANSLATIONS: List<TranslationMeta> = listOf(
            bundledEnglish(),
            bundledUrdu(),
        )
    }
}