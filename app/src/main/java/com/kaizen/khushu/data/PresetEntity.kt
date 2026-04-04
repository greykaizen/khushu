package com.kaizen.khushu.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "canvas_presets")
data class PresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val backgroundColor: Int,
    val widgets: List<CanvasWidget>,
    val isDeletable: Boolean
)

fun PresetEntity.toDomain() = CanvasPreset(id, name, backgroundColor, widgets, isDeletable)
fun CanvasPreset.toEntity() = PresetEntity(id, name, backgroundColor, widgets, isDeletable)