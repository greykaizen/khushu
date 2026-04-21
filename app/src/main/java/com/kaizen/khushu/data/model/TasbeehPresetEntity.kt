package com.kaizen.khushu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kaizen.khushu.data.local.TasbeehCanvasWidgetListConverter
import com.kaizen.khushu.ui.screens.tasbeeh.TasbihWidget

@Entity(tableName = "tasbeeh_canvas_presets")
@TypeConverters(TasbeehCanvasWidgetListConverter::class)
data class TasbeehPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val backgroundColor: Int,
    val widgets: List<TasbihWidget>,
    val isDeletable: Boolean = true
)

data class TasbeehCanvasPresetDomain(
    val id: String,
    val name: String,
    val backgroundColor: Int,
    val widgets: List<TasbihWidget>,
    val isDeletable: Boolean
)

fun TasbeehPresetEntity.toDomain() = TasbeehCanvasPresetDomain(id, name, backgroundColor, widgets, isDeletable)
fun TasbeehCanvasPresetDomain.toEntity() = TasbeehPresetEntity(id, name, backgroundColor, widgets, isDeletable)
