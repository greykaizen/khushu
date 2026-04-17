package com.kaizen.khushu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kaizen.khushu.data.local.entities.AyahEntity
import com.kaizen.khushu.data.local.entities.HadithEntity
import com.kaizen.khushu.data.local.entities.SurahEntity
import com.kaizen.khushu.data.local.entities.TranslationEntity

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        HadithEntity::class,
        TranslationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KhushuDatabase : RoomDatabase() {
    abstract fun ayahDao(): AyahDao
    abstract fun hadithDao(): HadithDao
}
