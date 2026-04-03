package com.kaizen.khushu.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "salah_canvas_layouts")
@TypeConverters(CanvasWidgetListConverter::class)
data class SalahCanvasLayout(
    @PrimaryKey val id: String = "default",  // single layout for now, extend later
    val backgroundColorInt: Int = 0xFF000000.toInt(),
    val widgets: List<CanvasWidget> = emptyList(),
)
