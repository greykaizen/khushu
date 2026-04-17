package com.kaizen.khushu.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "hadiths",
    primaryKeys = ["collection", "number"],
    indices = [Index(value = ["collection", "number"])]
)
data class HadithEntity(
    val collection: String,
    val number: Int,
    @ColumnInfo(name = "text_arabic")
    val textArabic: String? = null,
    @ColumnInfo(name = "text_en")
    val textEn: String,
    val grade: String,
    val narrator: String? = null
)
