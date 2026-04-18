package com.kaizen.khushu.data.model

data class TranslationMeta(
    val id: String,           // e.g. "eng-abdelhaleem"
    val langCode: String,     // ISO 639-1: "en", "ur", "tr", etc.
    val langName: String,     // "English"
    val translatorName: String, // "Abdel Haleem"
    val isRtl: Boolean,
    val downloadUrl: String,
    val sizeKb: Int,
)

// "en_20" is bundled in assets/translations/en_20.json (Sahih International, quran.com)
val AVAILABLE_TRANSLATIONS = listOf(
    TranslationMeta("en_20", "en", "English", "Sahih International", false,
        "", 926),  // bundled — no download URL needed
    TranslationMeta("en_19", "en", "English", "Pickthall", false,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/eng-mpickthall.json", 370),
    TranslationMeta("urd-fatehmuhammadja", "ur", "Urdu", "Fateh Muhammad Jalandhry", true,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/urd-fatehmuhammadja.json", 420),
    TranslationMeta("urd-muhammadjunagar", "ur", "Urdu", "Muhammad Junagarhi", true,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/urd-muhammadjunagar.json", 410),
    TranslationMeta("tur-diyanetisleri", "tr", "Turkish", "Diyanet İşleri", false,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/tur-diyanetisleri.json", 390),
    TranslationMeta("fra-muhammadhamidul", "fr", "French", "Muhammad Hamidullah", false,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/fra-muhammadhamidul.json", 400),
    TranslationMeta("deu-asfbubenheimand", "de", "German", "Bubenheim & Elyas", false,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/deu-asfbubenheimand.json", 410),
    TranslationMeta("ind-indonesianislam", "id", "Indonesian", "Kementerian Agama RI", false,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/ind-indonesianislam.json", 350),
    TranslationMeta("ben-muhiuddinkhan", "bn", "Bengali", "Muhiuddin Khan", false,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/ben-muhiuddinkhan.json", 430),
    TranslationMeta("rus-elmirkuliev", "ru", "Russian", "Elmir Kuliev", false,
        "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/rus-elmirkuliev.json", 420),
)
