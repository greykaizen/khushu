package com.kaizen.khushu.data.local

import androidx.room.Dao
import androidx.room.Query
import com.kaizen.khushu.data.local.entities.AyahEntity

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE surah = :surah AND ayah = :ayah LIMIT 1")
    suspend fun getAyah(surah: Int, ayah: Int): AyahEntity?

    @Query("SELECT * FROM ayahs WHERE surah = :surah ORDER BY ayah ASC")
    suspend fun getAyahsInSurah(surah: Int): List<AyahEntity>
}
