package com.kaizen.khushu.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey
    val number: Int,
    @ColumnInfo(name = "name_arabic")
    val nameArabic: String,
    @ColumnInfo(name = "name_en")
    val nameEn: String,
    @ColumnInfo(name = "name_translation")
    val nameTranslation: String,
    @ColumnInfo(name = "ayah_count")
    val ayahCount: Int,
    @ColumnInfo(name = "revelation_type")
    val revelationType: String // "meccan" or "medinan"
)
