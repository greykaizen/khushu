package com.kaizen.khushu.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbeehDao {
    @Query("SELECT * FROM tasbeeh_collections ORDER BY id ASC")
    fun getAll(): Flow<List<TasbeehCollection>>

    @Insert
    suspend fun insert(collection: TasbeehCollection)

    @Delete
    suspend fun delete(collection: TasbeehCollection)

    @Query("SELECT COUNT(*) FROM tasbeeh_collections")
    suspend fun count(): Int
}
