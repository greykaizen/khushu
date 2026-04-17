package com.kaizen.khushu.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "translations",
    primaryKeys = ["surah", "ayah", "lang", "edition"]
)
data class TranslationEntity(
    val surah: Int,
    val ayah: Int,
    val lang: String,
    val text: String,
    val edition: String
)
