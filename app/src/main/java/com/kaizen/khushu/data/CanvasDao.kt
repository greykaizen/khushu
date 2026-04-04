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

    // Presets
    @Query("SELECT * FROM canvas_presets")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity)

    @Query("UPDATE canvas_presets SET name = :newName WHERE id = :id")
    suspend fun renamePreset(id: String, newName: String)

    @Query("DELETE FROM canvas_presets WHERE id = :id")
    suspend fun deletePreset(id: String)

    @Query("SELECT COUNT(*) FROM canvas_presets")
    suspend fun getPresetCount(): Int
}
