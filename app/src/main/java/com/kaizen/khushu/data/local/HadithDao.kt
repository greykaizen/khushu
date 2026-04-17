package com.kaizen.khushu.data.local

import androidx.room.Dao
import androidx.room.Query
import com.kaizen.khushu.data.local.entities.HadithEntity

@Dao
interface HadithDao {
    @Query("SELECT * FROM hadiths WHERE collection = :collection AND number = :number LIMIT 1")
    suspend fun getHadith(collection: String, number: Int): HadithEntity?
}
