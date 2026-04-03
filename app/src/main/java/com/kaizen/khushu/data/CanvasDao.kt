package com.kaizen.khushu.data

import androidx.room.*
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CanvasDao {
    @Query("SELECT * FROM salah_canvas_layouts WHERE id = 'default'")
    fun getDefault(): Flow<SalahCanvasLayout?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(layout: SalahCanvasLayout)
}
