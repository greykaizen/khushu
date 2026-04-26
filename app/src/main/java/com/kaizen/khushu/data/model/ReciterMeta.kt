package com.kaizen.khushu.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull

@Serializable
data class ReciterMeta(
    val id: String,           // matches filename: "mishari", "abdulbaset", etc.
    val name: String,         // display name
    val style: String,        // "Murattal", "Mujawwad", etc.
    val audioUrlPattern: String = "",
    val source: String = "local", // "local", "mp3quran", "qf", "everyayah"
) {
    companion object {
        fun fromMp3QuranJson(obj: JsonObject): ReciterMeta {
            val id = obj["id"]?.jsonPrimitive?.intOrNull?.toString() ?: ""
            val server = obj["server"]?.jsonPrimitive?.content ?: ""
            return ReciterMeta(
                id = "mp3quran_$id",
                name = obj["name"]?.jsonPrimitive?.content ?: id,
                style = obj["rewaya"]?.jsonPrimitive?.content ?: "Hafs An Asim",
                // Build surah URL: server + surah_number_padded_to_3 + ".mp3"
                // e.g. server="https://server.mp3quran.net/afasy/" → "...001.mp3"
                audioUrlPattern = "${server}{surah_padded}.mp3",
                source = "mp3quran",
            )
        }

        fun fromQfJson(obj: JsonObject): ReciterMeta {
            val id = obj["id"]?.jsonPrimitive?.intOrNull?.toString() ?: ""
            return ReciterMeta(
                id = "qf_$id",
                name = obj["reciter_name"]?.jsonPrimitive?.content ?: id,
                style = obj["style"]?.jsonPrimitive?.content ?: "",
                audioUrlPattern = "qf:recitation:$id:{surah}",
                source = "qf",
            )
        }

        /**
         * everyayah.com has no machine-readable API. Reciters are listed on the site.
         * This hardcoded list covers the most common 10 reciters for Phase 1.
         * Per-ayah URL: https://everyayah.com/data/{folder}/{surah_3}{ayah_3}.mp3
         * e.g. https://everyayah.com/data/Mishary_Rashid_Alafasy_128kbps/001001.mp3
         */
        fun everyayahDefaults(): List<ReciterMeta> = listOf(
            ReciterMeta("everyayah_mishari",  "Mishary Al-Afasy",    "Hafs An Asim",
                "https://everyayah.com/data/Mishary_Rashid_Alafasy_128kbps/{surah_3}{ayah_3}.mp3", "everyayah"),
            ReciterMeta("everyayah_sudais",   "Abdul Rahman Al-Sudais", "Hafs An Asim",
                "https://everyayah.com/data/Abdurrahmaan_As-Sudais_192kbps/{surah_3}{ayah_3}.mp3", "everyayah"),
            ReciterMeta("everyayah_husary",   "Mahmoud Khalil Al-Husary", "Hafs An Asim",
                "https://everyayah.com/data/Husary_128kbps/{surah_3}{ayah_3}.mp3", "everyayah"),
            ReciterMeta("everyayah_minshawi", "Mohamed Siddiq Al-Minshawi", "Hafs An Asim",
                "https://everyayah.com/data/Minshawy_Murattal_128kbps/{surah_3}{ayah_3}.mp3", "everyayah"),
            ReciterMeta("everyayah_ghamdi",   "Saad Al-Ghamdi",      "Hafs An Asim",
                "https://everyayah.com/data/Ghamadi_40kbps/{surah_3}{ayah_3}.mp3", "everyayah"),
            ReciterMeta("everyayah_basfar",   "Abdullah Basfar",     "Hafs An Asim",
                "https://everyayah.com/data/Abdullah_Basfar_192kbps/{surah_3}{ayah_3}.mp3", "everyayah"),
            ReciterMeta("everyayah_ajamy",    "Ahmad Al-Ajamy",      "Hafs An Asim",
                "https://everyayah.com/data/ahmad_ibn_ali_al-ajamy_128kbps/{surah_3}{ayah_3}.mp3", "everyayah"),
            ReciterMeta("everyayah_tablawi",  "Mohammed Al-Tablawi",  "Hafs An Asim",
                "https://everyayah.com/data/Mohammad_al_Tablawi_128kbps/{surah_3}{ayah_3}.mp3", "everyayah"),
        )
    }
}

val AVAILABLE_RECITERS = listOf(
    ReciterMeta("mishari",    "Mishari Al-Afasy",          "Murattal", "", "local"),
    ReciterMeta("abdulbaset", "Abdul Baset",               "Mujawwad", "", "local"),
    ReciterMeta("sudais",     "Abdurrahman Al-Sudais",     "Murattal", "", "local"),
    ReciterMeta("husary",     "Mahmoud Khalil Al-Husary",  "Murattal", "", "local"),
    ReciterMeta("minshawi",   "Mohamed Siddiq Al-Minshawi","Murattal", "", "local"),
)