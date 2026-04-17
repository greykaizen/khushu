package com.kaizen.khushu.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "ayahs",
    primaryKeys = ["surah", "ayah"],
    foreignKeys = [
        ForeignKey(
            entity = SurahEntity::class,
            parentColumns = ["number"],
            childColumns = ["surah"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["surah", "ayah"])]
)
data class AyahEntity(
    val surah: Int,
    val ayah: Int,
    @ColumnInfo(name = "text_uthmani")
    val textUthmani: String,
    @ColumnInfo(name = "tajweed_markup")
    val tajweedMarkup: String? = null
)
