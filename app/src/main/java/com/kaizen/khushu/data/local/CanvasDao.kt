package com.kaizen.khushu.data.local

import androidx.room.*
import com.kaizen.khushu.ui.screens.salah.SalahCanvasLayout
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehCanvasLayout
import com.kaizen.khushu.data.model.PresetEntity
import com.kaizen.khushu.data.model.TasbeehPresetEntity
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

    // --- Tasbeeh Layouts ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTasbeeh(layout: TasbeehCanvasLayout)

    @Query("SELECT * FROM tasbeeh_canvas_layouts WHERE id = 'default'")
    fun getTasbeehDefault(): Flow<TasbeehCanvasLayout?>

    // --- Salah Presets ---
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

    @Query("SELECT * FROM canvas_presets WHERE id = :id")
    fun getPresetById(id: String): Flow<PresetEntity?>

    // --- Tasbeeh Presets ---
    @Query("SELECT * FROM tasbeeh_canvas_presets")
    fun getAllTasbeehPresets(): Flow<List<TasbeehPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbeehPreset(preset: TasbeehPresetEntity)

    @Query("UPDATE tasbeeh_canvas_presets SET name = :newName WHERE id = :id")
    suspend fun renameTasbeehPreset(id: String, newName: String)

    @Query("DELETE FROM tasbeeh_canvas_presets WHERE id = :id")
    suspend fun deleteTasbeehPreset(id: String)

    @Query("SELECT COUNT(*) FROM tasbeeh_canvas_presets")
    suspend fun getTasbeehPresetCount(): Int

    @Query("SELECT * FROM tasbeeh_canvas_presets WHERE id = :id")
    fun getTasbeehPresetById(id: String): Flow<TasbeehPresetEntity?>

    @Query("DELETE FROM canvas_presets")
    suspend fun deleteAllPresets()
}
