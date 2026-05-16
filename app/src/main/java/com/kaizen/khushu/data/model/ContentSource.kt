package com.kaizen.khushu.data.model

enum class ContentSource(
    val displayName: String,
    val shortLabel: String,
    val supportsTranslations: Boolean = false,
    val supportsTafsir: Boolean = false,
    val supportsAudio: Boolean = false,
    val supportsPerAyahAudio: Boolean = false,
    val supportsReflections: Boolean = false,
) {
    QF(
        displayName = "Quran Foundation",
        shortLabel = "QF",
        supportsTranslations = true,
        supportsTafsir = true,
        supportsAudio = true,
        supportsPerAyahAudio = true,
        supportsReflections = true,
    ),
    FAWAZ(
        displayName = "fawazahmed0",
        shortLabel = "fawaz",
        supportsTranslations = true,
    ),
    QURANENC(
        displayName = "QuranEnc",
        shortLabel = "QuranEnc",
        supportsTranslations = true,
    ),
    SPA5K(
        displayName = "spa5k / GitHub",
        shortLabel = "spa5k",
        supportsTafsir = true,
    ),
    MP3QURAN(
        displayName = "mp3quran.net",
        shortLabel = "mp3quran",
        supportsAudio = true,
        supportsPerAyahAudio = false,  // surah-level only
    ),
    EVERYAYAH(
        displayName = "everyayah.com",
        shortLabel = "everyayah",
        supportsAudio = true,
        supportsPerAyahAudio = true,   // primary source for in-salah per-ayah playback
    ),
}